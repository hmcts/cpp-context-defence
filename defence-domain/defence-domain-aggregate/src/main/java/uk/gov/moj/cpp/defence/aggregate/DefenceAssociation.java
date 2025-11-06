package uk.gov.moj.cpp.defence.aggregate;

import static java.util.Optional.empty;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;
import static uk.gov.moj.cpp.defence.common.util.GrantAccessUtil.preparePermissionList;

import org.apache.commons.collections.map.HashedMap;
import uk.gov.justice.cps.defence.Permission;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.defence.events.DefenceAssociationFailed;
import uk.gov.moj.cpp.defence.events.DefenceDisassociationFailed;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationAssociated;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationAssociationUnlockedBdf;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationLaareferenceReceived;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationDisassociated;
import uk.gov.moj.cpp.defence.events.DefendantDefenceAssociationLockedForLaa;
import uk.gov.moj.cpp.defence.events.DefendantLegalStatusUpdated;
import uk.gov.moj.cpp.defence.events.RepresentationType;
import uk.gov.moj.cpp.defence.events.Status;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("pmd:NullAssignment")
public class DefenceAssociation implements Aggregate {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefenceAssociation.class);
    private static final long serialVersionUID = 4984453170269637134L;
    private static final String UTC = "UTC";
    private static final ZoneId UTC_ZONE_ID = ZoneId.of(UTC);
    private UUID associatedOrganisationId;
    private List<UUID> disassociatedOrganisationIds;
    private boolean isLockedByRepOrder = false;

    private Boolean isAssociatedByRepOrder = false;



    private String laaContractNumber;
    @SuppressWarnings("squid:S1948")
    private List<Permission> permissionIdList = new ArrayList<>();

    private Map<UUID, String> orgIdLegalStatusMap = new HashedMap();


    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(DefenceOrganisationAssociated.class).apply(e ->
                        {
                            this.associatedOrganisationId = e.getOrganisationId();
                            this.permissionIdList.addAll(e.getPermissions());
                            this.isLockedByRepOrder = false;
                            this.isAssociatedByRepOrder = e.getIsLAA();
                        }
                ),
                when(DefenceOrganisationDisassociated.class).apply(e ->
                        {
                            populatedDisassociatedOrganisationIds(e);
                            this.associatedOrganisationId = null;
                            this.permissionIdList.clear();
                        }
                ),
                when(DefendantDefenceAssociationLockedForLaa.class).apply(e -> {
                    this.isLockedByRepOrder = true;
                    this.laaContractNumber = e.getLaaContractNumber();
                    this.associatedOrganisationId = null;
                }),
                when(DefenceOrganisationAssociationUnlockedBdf.class).apply(e -> {
                    this.isLockedByRepOrder = false;
                    this.laaContractNumber = null;
                    this.associatedOrganisationId = e.getOrganisationId();
                    this.permissionIdList.addAll(e.getPermissions());
                }),
                when(DefendantLegalStatusUpdated.class).apply(e ->
                    orgIdLegalStatusMap.put(e.getOrganisationId(), e.getLegalAidStatus())
                ),
                otherwiseDoNothing()
        );
    }

    private void populatedDisassociatedOrganisationIds(final DefenceOrganisationDisassociated e) {
        if (disassociatedOrganisationIds == null) {
            disassociatedOrganisationIds = new ArrayList<>();
        }
        this.disassociatedOrganisationIds.add(e.getOrganisationId());
    }

    public Stream<Object> associateOrganisation(final UUID defendantId, final UUID organisationId,
                                                final String organisationName,
                                                final String representationType,
                                                final String laaContractNumber,
                                                final String userId) {
        LOGGER.debug("Starting to associate a defence organisation to defendant");
        final Stream.Builder<Object> streamBuilder = Stream.builder();

        if (organisationId != null && !organisationId.equals(associatedOrganisationId) && !isLockedByRepOrder) {

            final String legalAidStatus = orgIdLegalStatusMap.get(organisationId);
            streamBuilder.add(DefenceOrganisationAssociated.defenceOrganisationAssociated()
                    .withOrganisationId(organisationId)
                    .withOrganisationName(organisationName)
                    .withDefendantId(defendantId)
                    .withStartDate(ZonedDateTime.now(UTC_ZONE_ID))
                    .withRepresentationType(RepresentationType.valueOf(representationType))
                    .withLaaContractNumber(laaContractNumber)
                    .withLegalAidStatus(legalAidStatus)
                    .withUserId(UUID.fromString(userId))
                    .withPermissions(preparePermissionList(defendantId, organisationId, true, empty()))
                    .build());
        } else {
            streamBuilder.add(DefenceAssociationFailed.defenceAssociationFailed().withDefendantId(defendantId).withFailureReason("Defence organisation can not be associated").build());
        }
        return apply(streamBuilder.build());
    }

    public Stream<Object> unlockDefenceOrganisationAssociationBDF(final UUID defendantId, final UUID organisationId) {
        LOGGER.info("Starting to unlock association of defence organisation {} with defendant {}", organisationId, defendantId);
        final Stream.Builder<Object> streamBuilder = Stream.builder();
        streamBuilder.add(DefenceOrganisationAssociationUnlockedBdf.defenceOrganisationAssociationUnlockedBdf()
                .withOrganisationId(organisationId)
                .withDefendantId(defendantId)
                .withPermissions(preparePermissionList(defendantId, organisationId, true, empty()))
                .build());
        return apply(streamBuilder.build());
    }
    public Stream<Object> disassociateOrganisation(final UUID defendantId, final UUID organisationId, final UUID caseId, final UUID userId, final Boolean isLaa) {
        LOGGER.debug("Starting to disassociate a defence organisation from defendant");

        if (this.associatedOrganisationId == null) {
            return raiseDisassociationFailedEvent(defendantId, "No Association found for Defence client");
        } else if (!this.associatedOrganisationId.equals(organisationId)) {
            return raiseDisassociationFailedEvent(defendantId, String.format("Organisation id '%s' is not currently associated with Defence client", organisationId));
        }

        final String legalAidStatus = orgIdLegalStatusMap.get(organisationId);
        return apply(Stream.of(DefenceOrganisationDisassociated.defenceOrganisationDisassociated()
                .withOrganisationId(organisationId)
                .withCaseId(caseId)
                .withDefendantId(defendantId)
                .withLegalAidStatus(legalAidStatus)
                .withUserId(userId)
                .withEndDate(ZonedDateTime.now(UTC_ZONE_ID))
                .withPermissions(preparePermissionListForStatus(Status.DELETED))
                .withIsLAA(isLaa)
                .build()));
    }

    public Stream<Object> raiseDisassociationFailedEvent(final UUID defendantId, final String message) {
        return apply(Stream.of(
                DefenceDisassociationFailed.defenceDisassociationFailed()
                        .withDefendantId(defendantId)
                        .withFailureReason(message)
                        .build()
                )
        );
    }

    public Stream<Object> associateOrganisationForRepOrder(final UUID defendantId, final UUID organisationId,
                                                           final String organisationName,
                                                           final String representationType,
                                                           final String laaContractNumber,
                                                           final String userId,
                                                           final UUID caseId) {
        LOGGER.debug("Starting to associate a defence organisation to defendant by Representation Order");
        final Stream.Builder<Object> streamBuilder = Stream.builder();
        final String legalAidStatus = orgIdLegalStatusMap.get(organisationId);

        if(organisationId.equals(this.associatedOrganisationId)){
            if(null == isAssociatedByRepOrder || !isAssociatedByRepOrder) {
                streamBuilder.add(DefenceOrganisationLaareferenceReceived.defenceOrganisationLaareferenceReceived()
                        .withOrganisationId(organisationId)
                        .withOrganisationName(organisationName)
                        .withDefendantId(defendantId)
                        .withStartDate(ZonedDateTime.now(UTC_ZONE_ID))
                        .withRepresentationType(RepresentationType.valueOf(representationType))
                        .withLaaContractNumber(laaContractNumber)
                        .withUserId(UUID.fromString(userId))
                        .withCaseId(caseId)
                        .withIsLAA(true)
                        .build());
            }
            return apply(streamBuilder.build());
        }
        if (this.associatedOrganisationId != null) {
            streamBuilder.add(DefenceOrganisationDisassociated.defenceOrganisationDisassociated()
                    .withOrganisationId(associatedOrganisationId)
                    .withDefendantId(defendantId)
                    .withUserId(UUID.fromString(userId))
                    .withEndDate(ZonedDateTime.now(UTC_ZONE_ID))
                    .withPermissions(preparePermissionListForStatus(Status.DELETED))
                    .withCaseId(caseId)
                    .withIsLAA(true)
                    .build());
        }
        streamBuilder.add(DefenceOrganisationAssociated.defenceOrganisationAssociated()
                .withOrganisationId(organisationId)
                .withOrganisationName(organisationName)
                .withDefendantId(defendantId)
                .withStartDate(ZonedDateTime.now(UTC_ZONE_ID))
                .withRepresentationType(RepresentationType.valueOf(representationType))
                .withLaaContractNumber(laaContractNumber)
                .withUserId(UUID.fromString(userId))
                .withPermissions(preparePermissionList(defendantId, organisationId,true, empty()))
                .withCaseId(caseId)
                .withLegalAidStatus(legalAidStatus)
                .withIsLAA(true)
                .build());

        return apply(streamBuilder.build());

    }

    public Stream<Object> handleOrphanedDefendantAssociation(final UUID organisationId, final String organisationName,
                                                             final UUID defendantId, final String laaContractNumber, final String userId) {
        final Stream.Builder<Object> streamBuilder = Stream.builder();
        //Raise event to set the is Associated flag to true for orphaned Defendant as it is associated now
        if (this.associatedOrganisationId != null) {
            streamBuilder.add(DefenceOrganisationDisassociated.defenceOrganisationDisassociated()
                    .withOrganisationId(associatedOrganisationId)
                    .withDefendantId(defendantId)
                    .withUserId(UUID.fromString(userId))
                    .withEndDate(ZonedDateTime.now(UTC_ZONE_ID))
                    .withPermissions(preparePermissionListForStatus(Status.DELETED))
                    .build());
        }

        streamBuilder.add(DefenceOrganisationAssociated.defenceOrganisationAssociated()
                .withOrganisationId(organisationId)
                .withOrganisationName(organisationName)
                .withDefendantId(defendantId)
                .withStartDate(ZonedDateTime.now(UTC_ZONE_ID))
                .withRepresentationType(RepresentationType.REPRESENTATION_ORDER)
                .withLaaContractNumber(laaContractNumber)
                .withUserId(UUID.fromString(userId))
                .withPermissions(preparePermissionList(defendantId, organisationId, true, empty()))
                .build());
        return apply(streamBuilder.build());
    }

    public Stream<Object> handleDefendantDefenceAssociationLocked(final UUID defendantId, final String laaContractNumber) {
        LOGGER.debug("Applying lock on defendant for association so that it can't be associated by any other defence organisation {}", defendantId);
        return apply(Stream.of(DefendantDefenceAssociationLockedForLaa.defendantDefenceAssociationLockedForLaa()
                .withDefendantId(defendantId)
                .withLaaContractNumber(laaContractNumber)
                .build()));
    }

    public UUID getAssociatedOrganisationId() {
        return associatedOrganisationId;
    }

    private List<Permission> preparePermissionListForStatus(final Status status) {
        return permissionIdList.stream()
                .map(permission ->
                        Permission.permission()
                                .withId(permission.getId())
                                .withTarget(permission.getTarget())
                                .withObject(permission.getObject())
                                .withAction(permission.getAction())
                                .withSource(permission.getSource())
                                .withStatus(status)
                                .build()
                ).collect(Collectors.toList());
    }

    public String getLaaContractNumber() {
        return laaContractNumber;
    }

    public Stream<Object> recordLegalStatusForDefendant(final UUID defendantId, final UUID orgId, final String legalStatusUpdated) {
        return apply(Stream.of(DefendantLegalStatusUpdated.defendantLegalStatusUpdated()
                .withDefendantId(defendantId)
                .withOrganisationId(orgId)
                .withLegalAidStatus(legalStatusUpdated)
                .build()));
    }
}
