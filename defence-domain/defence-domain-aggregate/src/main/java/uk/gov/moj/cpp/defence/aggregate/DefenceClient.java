package uk.gov.moj.cpp.defence.aggregate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.of;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.ALREADY_GRANTED;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.PERMISSION_NOT_FOUND;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.UNAUTHORIZED_GRANTING;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.UNAUTHORIZED_REMOVE_GRANTING;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.USER_NOT_FOUND;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.USER_NOT_IN_ALLOWED_GROUPS;
import static uk.gov.moj.cpp.defence.common.util.GrantAccessUtil.isGranteeInAllowedGroupsToGrantAccess;
import static uk.gov.moj.cpp.defence.common.util.GrantAccessUtil.isUserBelongsToAssociatedOrganisation;
import static uk.gov.moj.cpp.defence.common.util.GrantAccessUtil.isUserHasPermissionToGrantSomeone;
import static uk.gov.moj.cpp.defence.common.util.GrantAccessUtil.isUserHasPermissionToRemoveGrantAccess;
import static uk.gov.moj.cpp.defence.common.util.GrantAccessUtil.preparePermissionList;
import static uk.gov.moj.cpp.defence.events.IdpcAccessByOrganisationRecorded.idpcAccessByOrganisationRecorded;
import static uk.gov.moj.cpp.defence.events.IdpcAccessRecorded.idpcAccessRecorded;

import uk.gov.justice.cps.defence.AllegationsReceivedAgainstADefenceClient;
import uk.gov.justice.cps.defence.AssigneeForDefenceIsProsecutingCase;
import uk.gov.justice.cps.defence.DefenceClientDetails;
import uk.gov.justice.cps.defence.DefendantDetails;
import uk.gov.justice.cps.defence.GranteeUserNotInAllowedGroups;
import uk.gov.justice.cps.defence.Offence;
import uk.gov.justice.cps.defence.OffenceCodeReferenceData;
import uk.gov.justice.cps.defence.Permission;
import uk.gov.justice.cps.defence.PersonDetails;
import uk.gov.justice.cps.defence.UserAlreadyGranted;
import uk.gov.justice.cps.defence.UserNotFound;
import uk.gov.justice.cps.defence.event.ReceiveDefendantUpdateFailed;
import uk.gov.justice.cps.defence.events.AccessGrantRemoved;
import uk.gov.justice.cps.defence.events.AccessGranted;
import uk.gov.justice.cps.defence.events.GrantAccessFailed;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.defence.IdpcDetails;
import uk.gov.moj.cpp.defence.Organisation;
import uk.gov.moj.cpp.defence.common.util.ErrorType;
import uk.gov.moj.cpp.defence.event.listener.events.AddedOffences;
import uk.gov.moj.cpp.defence.event.listener.events.DefendantOffencesUpdated;
import uk.gov.moj.cpp.defence.event.listener.events.DefendantUpdateReceived;
import uk.gov.moj.cpp.defence.event.listener.events.DeletedOffences;
import uk.gov.moj.cpp.defence.events.DefenceClientDoesNotExist;
import uk.gov.moj.cpp.defence.events.DefenceClientReceived;
import uk.gov.moj.cpp.defence.events.DefenceClientUrnAdded;
import uk.gov.moj.cpp.defence.events.IdpcAccessByOrganisationRecorded;
import uk.gov.moj.cpp.defence.events.IdpcAccessRecorded;
import uk.gov.moj.cpp.defence.events.IdpcDetailsRecorded;
import uk.gov.moj.cpp.defence.events.IdpcReceivedBeforeCase;
import uk.gov.moj.cpp.defence.events.InstructionDetailsRecorded;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings("pmd:NullAssignment")
public class DefenceClient implements Aggregate {

    private static final long serialVersionUID = -7950876778016335076L;
    private final Set<UUID> organisationsInstructed = new HashSet<>();
    @SuppressWarnings("squid:S1948")
    private final Map<UUID, List<Permission>> userPermissionMap = new HashMap<>();
    private UUID defenceClientId;
    private String defenceClientSurname;
    private String defenceClientOrganisationName;
    private String caseUrn;
    private UUID lastOrganisationToAccessIdpc;
    private UUID caseId;
    private UUID defendantId;
    private IdpcDetails pendingIdpc;

    public Stream<Object> removeAllGrantees() {

        final Stream.Builder<Object> streamBuilder = Stream.builder();

        userPermissionMap.forEach((granteeUserId, permissionList) ->
                streamBuilder.add(AccessGrantRemoved.accessGrantRemoved()
                        .withPermissions(permissionList)
                        .withDefendantId(defendantId)
                        .withGranteeUserId(granteeUserId)
                        .build())
        );

        return apply(streamBuilder.build());
    }

    public Stream<Object> removeGrantAccessToUser(final UUID granteeUserId, final UUID loggedInUserId, final UUID associatedOrganisationId, final Organisation loggedInUserOrganisation, final Organisation granteeOrganisation, final List<String> loggedInUserGroupList) {

        if (!isUserHasPermissionToRemoveGrantAccess(granteeUserId, loggedInUserId, associatedOrganisationId, loggedInUserOrganisation, granteeOrganisation, loggedInUserGroupList)) {
            return apply(of(getFailEvent(UNAUTHORIZED_REMOVE_GRANTING, loggedInUserId)));
        }

        if (!userPermissionMap.containsKey(granteeUserId)) {
            return apply(of(getFailEvent(PERMISSION_NOT_FOUND, loggedInUserId)));
        }

        return apply(of(AccessGrantRemoved.accessGrantRemoved()
                .withPermissions(userPermissionMap.get(granteeUserId))
                .withDefendantId(defendantId)
                .withGranteeUserId(granteeUserId)
                .build()));
    }

    public Stream<Object> grantAccessToUser(final UUID target, final String granteeEmail, final PersonDetails granteeDetails, final List<String> granteeGroupList, final List<String> granterGroupList, final Organisation granteeOrganisation, final Organisation granterOrganisation, final PersonDetails granterDetails, final UUID associatedOrganisationId, final boolean isInProsecutorRole) {


        if (granteeDetails == null) {
            return apply(
                    of(
                            UserNotFound.userNotFound()
                                    .withEmail(granteeEmail)
                                    .withFailureReason(USER_NOT_FOUND.getMessage())
                                    .build()
                    ));
        }

        if (isInProsecutorRole) {
            return apply(
                    of(
                            AssigneeForDefenceIsProsecutingCase.assigneeForDefenceIsProsecutingCase()
                                    .withEmail(granteeEmail)
                                    .withCaseId(this.caseId)
                                    .withUserId(granteeDetails.getUserId())
                                    .build()
                    ));
        }



        if (granteeOrganisation == null) {
            return apply(
                    of(
                            GranteeUserNotInAllowedGroups.granteeUserNotInAllowedGroups()
                                    .withEmail(granteeEmail)
                                    .withFailureReason(USER_NOT_IN_ALLOWED_GROUPS.getMessage())
                                    .build()
                    ));
        }

        if (!isUserHasPermissionToGrantSomeone(associatedOrganisationId, granterOrganisation.getOrgId(), granteeOrganisation.getOrgId(), granterGroupList)) {
            return apply(of(getFailEvent(UNAUTHORIZED_GRANTING, granterDetails.getUserId())));
        }


        if (isAlreadyGranted(granteeDetails.getUserId(), associatedOrganisationId, granteeOrganisation)) {
            return apply(
                    of(
                            UserAlreadyGranted.userAlreadyGranted()
                                    .withEmail(granteeEmail)
                                    .withFailureReason(ALREADY_GRANTED.getMessage())
                                    .build()
                    ));
        }

        if (!isGranteeInAllowedGroupsToGrantAccess(granteeGroupList)) {
            return apply(
                    of(
                            GranteeUserNotInAllowedGroups.granteeUserNotInAllowedGroups()
                                    .withEmail(granteeEmail)
                                    .withFailureReason(USER_NOT_IN_ALLOWED_GROUPS.getMessage())
                                    .build()
                    ));
        }

        return apply(of(AccessGranted.accessGranted()
                .withPermissions(preparePermissionList(target, granteeDetails.getUserId(), false, Optional.of(granteeGroupList)))
                .withGranteeDetails(granteeDetails)
                .withGranteeOrganisation(granteeOrganisation)
                .withGranterDetails(granterDetails)
                .withDefendantId(target)
                .build()));
    }

    public boolean isAlreadyGranted(final UUID granteeUserId, final UUID associatedOrganisationId, final Organisation granteeOrganisation) {
        return this.userPermissionMap.containsKey(granteeUserId) || isUserBelongsToAssociatedOrganisation(associatedOrganisationId, granteeOrganisation.getOrgId());
    }

    public Stream<Object> receiveADefenceClient(final UUID defenceClientId, final String urn,
                                                final DefendantDetails defendantDetails, final DefenceClientDetails defenceClientDetails,
                                                final UUID defendantId) {

        final Stream.Builder<Object> eventStreamBuilder = Stream.builder();

        eventStreamBuilder.add(DefenceClientReceived.defenceClientReceived()
                .withDefenceClientDetails(defenceClientDetails)
                .withDefendantDetails(defendantDetails)
                .withDefenceClientId(defenceClientId)
                .withUrn(urn)
                .withDefendantId(defendantId)
                .build());

        if(nonNull(this.pendingIdpc)){
            eventStreamBuilder.add(IdpcDetailsRecorded.idpcDetailsRecorded()
                    .withDefenceClientId(defenceClientId)
                    .withIdpcDetails(this.pendingIdpc)
                    .build());
        }

        return apply(eventStreamBuilder.build());
    }

    public Stream<Object> receiveUpdateClient(final DefendantDetails defendantDetails, final UUID defendantId) {

        if (isNull(caseId)) {
            return apply(of(ReceiveDefendantUpdateFailed.receiveDefendantUpdateFailed()
                    .withCaseId(defendantDetails.getCaseId())
                    .withDefendantId(defendantId)
                    .withErrorMessage("Defence client Id could not be found!")
                    .build()));
        }

        final DefendantUpdateReceived defendantUpdateReceived = DefendantUpdateReceived.defendantUpdateReceived()
                .withDefendantDetails(defendantDetails)
                .withDefendantId(defendantId)
                .build();

        return apply(of(defendantUpdateReceived));
    }

    public Stream<Object> receiveAllegations(final UUID defenceClientId,
                                             final UUID defendantId,
                                             final DefendantDetails defendantDetails,
                                             final String policeDefendantId,
                                             final List<Offence> offences,
                                             final List<OffenceCodeReferenceData> offenceCodeReferenceData) {

        return apply(
                of(
                        AllegationsReceivedAgainstADefenceClient.allegationsReceivedAgainstADefenceClient()
                                .withDefenceClientId(defenceClientId)
                                .withDefendantDetails(defendantDetails)
                                .withDefendantId(defendantId)
                                .withOffences(offences)
                                .withPoliceDefendantId(policeDefendantId)
                                .withOffenceCodeReferenceData(offenceCodeReferenceData)
                                .build()));
    }

    public Stream<Object> receiveUpdateOffences(final LocalDate modifiedDate, final List<AddedOffences> addedOffences, final List<DeletedOffences> deletedOffences) {

        final DefendantOffencesUpdated.Builder defendantOffencesUpdatedBuilder = DefendantOffencesUpdated.defendantOffencesUpdated();
        defendantOffencesUpdatedBuilder.withAddedOffences(addedOffences)
                .withModifiedDate(modifiedDate);
        if (deletedOffences != null && !deletedOffences.isEmpty()) {
            defendantOffencesUpdatedBuilder.withDeletedOffences(deletedOffences);
        }
        return apply(
                of(defendantOffencesUpdatedBuilder.build()
                ));
    }

    public Stream<Object> recordInstructionDetails(final LocalDate instructionDate, final UUID userId, final UUID organisationId, final UUID instructionId, final UUID defenceClientId) {

        if (this.defenceClientId == null) {
            return apply(of(new DefenceClientDoesNotExist(defenceClientId, this.defendantId)));
        } else {
            final InstructionDetailsRecorded instructionDetailsRecorded = InstructionDetailsRecorded.instructionDetailsRecorded()
                    .withInstructionDate(instructionDate)
                    .withInstructionId(instructionId)
                    .withDefenceClientId(this.defenceClientId)
                    .withOrganisationId(organisationId)
                    .withUserId(userId)
                    .withDefendantId(this.defendantId)
                    .withCaseId(this.caseId)
                    .withFirstInstruction(!this.organisationsInstructed.contains(organisationId))
                    .build();
            return apply(of(instructionDetailsRecorded));

        }
    }

    public Stream<Object> receiveUrn(final UUID defenceClientId, final String urn) {
        return apply(of(DefenceClientUrnAdded.defenceClientUrnAdded()
                .withDefenceClientId(defenceClientId)
                .withUrn(urn)
                .build()));
    }

    public Stream<Object> recordIdpcDetails(final IdpcDetails idpcDetails, final UUID defenceClientId) {

        if (isNull(this.defenceClientId)) {
            return apply(of(IdpcReceivedBeforeCase.idpcReceivedBeforeCase()
                    .withDefenceClientId(defenceClientId)
                    .withIdpcDetails(idpcDetails)
                    .build()));
        }

        return apply(of(IdpcDetailsRecorded.idpcDetailsRecorded()
                .withDefenceClientId(defenceClientId)
                .withIdpcDetails(idpcDetails)
                .build()));
    }

    public Stream<Object> recordIdpcAccess(final ZonedDateTime accessTimestamp, final UUID userId, final UUID organisationId, final UUID idpcDetailsId, final UUID materialId) {

        final Stream.Builder streamBuilder = Stream.builder();

        streamBuilder.add(idpcAccessRecorded()
                .withAccessTimestamp(accessTimestamp)
                .withDefenceClientId(this.defenceClientId)
                .withUserId(userId)
                .withOrganisationId(organisationId)
                .withIdpcDetailsId(idpcDetailsId)
                .withMaterialId(materialId)
                .build());

        if (isNewOrganisationAccess(organisationId)) {
            streamBuilder.add(idpcAccessByOrganisationRecorded()
                    .withAccessTimestamp(accessTimestamp)
                    .withDefenceClientId(this.defenceClientId)
                    .withUserId(userId)
                    .withOrganisationId(organisationId)
                    .withMaterialId(materialId)
                    .withDefenceClientSurname(defenceClientSurname)
                    .withDefenceClientOrganisationName(defenceClientOrganisationName)
                    .withCaseUrn(caseUrn)
                    .build());
        }

        return apply(streamBuilder.build());
    }

    private boolean isNewOrganisationAccess(final UUID organisationId) {
        return (null == lastOrganisationToAccessIdpc || !lastOrganisationToAccessIdpc.equals(organisationId));
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(DefenceClientReceived.class)
                        .apply(defenceClientReceived -> {
                            this.defenceClientId = defenceClientReceived.getDefenceClientId();
                            this.caseUrn = defenceClientReceived.getUrn();
                            this.caseId = defenceClientReceived.getDefendantDetails().getCaseId();
                            this.defendantId = defenceClientReceived.getDefendantId();
                            if (nonNull(defenceClientReceived.getDefendantDetails().getOrganisation())) {
                                this.defenceClientOrganisationName = defenceClientReceived.getDefendantDetails().getOrganisation().getOrganisationName();
                            } else {
                                this.defenceClientSurname = defenceClientReceived.getDefendantDetails().getLastName();
                            }
                        }),
                when(DefenceClientDoesNotExist.class)
                        .apply(defenceClientDoesNotExist -> {
                            //do nothing
                        }),
                when(AllegationsReceivedAgainstADefenceClient.class)
                        .apply(defenceClientIsCharged -> {
                            //do nothing
                        }),
                when(IdpcDetailsRecorded.class)
                        .apply(idpcDetailsRecorded ->
                                this.pendingIdpc = null
                        ),
                when(InstructionDetailsRecorded.class)
                        .apply(instructionDetailsRecorded ->
                                organisationsInstructed.add(instructionDetailsRecorded.getOrganisationId())
                        ),
                when(IdpcAccessRecorded.class)
                        .apply(idpcAccessRecorded -> {
                        }),
                when(IdpcAccessByOrganisationRecorded.class)
                        .apply(idpcAccessByOrganisationRecorded ->
                                this.lastOrganisationToAccessIdpc = idpcAccessByOrganisationRecorded.getOrganisationId()
                        ),
                when(DefenceClientUrnAdded.class)
                        .apply(defenceClientUrnAdded ->
                                this.caseUrn = defenceClientUrnAdded.getUrn()
                        ),
                when(DefendantUpdateReceived.class)
                        .apply(defendantUpdateReceived -> {
                                    if (defendantUpdateReceived.getDefendantDetails().getLastName() != null) {
                                        this.defenceClientSurname = defendantUpdateReceived.getDefendantDetails().getLastName();
                                    }
                                }
                        ),
                when(DefendantOffencesUpdated.class)
                        .apply(defendantOffencesUpdated -> {
                            //do nothing
                        }),
                when(AccessGranted.class)
                        .apply(accessGranted ->
                                this.userPermissionMap.put(accessGranted.getGranteeDetails().getUserId(), accessGranted.getPermissions())
                        ),
                when(UserNotFound.class)
                        .apply(userNotFound -> {
                            //do nothing
                        }),
                when(GranteeUserNotInAllowedGroups.class)
                        .apply(granteeUserNotInAllowedGroups -> {
                            //do nothing
                        }),
                when(UserAlreadyGranted.class)
                        .apply(userAlreadyGranted -> {
                            //do nothing
                        }),
                when(GrantAccessFailed.class)
                        .apply(userNotAuthorised -> {
                            //do nothing
                        }),
                when(AccessGrantRemoved.class)
                        .apply(accessGrantRemoved ->
                                this.userPermissionMap.remove(accessGrantRemoved.getGranteeUserId())
                        ),
                when(IdpcReceivedBeforeCase.class)
                        .apply(idpcReceivedBeforeCase ->
                                this.pendingIdpc = idpcReceivedBeforeCase.getIdpcDetails()
                        ),
                otherwiseDoNothing()

        );
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    private GrantAccessFailed getFailEvent(final ErrorType errorType, final UUID userId) {
        return GrantAccessFailed.grantAccessFailed()
                .withUserId(userId)
                .withErrorCode(errorType.getCode())
                .withErrorMessage(errorType.getMessage())
                .build();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getCaseUrn() {
        return this.caseUrn;
    }


}
