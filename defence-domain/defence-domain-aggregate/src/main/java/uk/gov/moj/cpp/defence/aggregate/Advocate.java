package uk.gov.moj.cpp.defence.aggregate;

import static java.lang.Integer.parseInt;
import static java.time.ZonedDateTime.now;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.builder;
import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;
import static uk.gov.justice.cps.defence.CaseAssignmentsByHearingListingFailed.caseAssignmentsByHearingListingFailed;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;
import static uk.gov.moj.cpp.defence.aggregate.util.CaseAssignmentHelper.buildCaseAssignedToAdvocate;
import static uk.gov.moj.cpp.defence.aggregate.util.CaseAssignmentHelper.buildCaseAssignedToOrganisation;
import static uk.gov.moj.cpp.defence.aggregate.util.CaseAssignmentHelper.buildCasesAssignedToAdvocate;
import static uk.gov.moj.cpp.defence.aggregate.util.CaseAssignmentHelper.buildCasesAssignedToOrganisation;
import static uk.gov.moj.cpp.defence.aggregate.util.CaseAssignmentHelper.toAssignmentError;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.ALREADY_ASSIGNED;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.ASSIGNEE_DEFENDING_CASE;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.ASSIGNEE_NOT_IN_ALLOWED_GROUPS;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.USER_NOT_ASSIGNED;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.USER_NOT_FOUND;
import static uk.gov.moj.cpp.defence.common.util.ErrorType.valueOf;
import static uk.gov.moj.cpp.defence.common.util.GrantAccessUtil.isAssigneeInAllowedGroupsToGetCaseAccess;

import uk.gov.justice.cps.defence.AssigneeForProsecutionIsDefendingCase;
import uk.gov.justice.cps.defence.AssigneeNotInAllowedGroups;
import uk.gov.justice.cps.defence.AssignmentError;
import uk.gov.justice.cps.defence.CaseAssignmentsByHearingListingFailed;
import uk.gov.justice.cps.defence.PersonDetails;
import uk.gov.justice.cps.defence.UserAlreadyAssigned;
import uk.gov.justice.cps.defence.UserNotAssigned;
import uk.gov.justice.cps.defence.UserNotFound;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.defence.Organisation;
import uk.gov.moj.cpp.defence.common.util.ErrorType;
import uk.gov.moj.cpp.defence.events.CaseAssigmentToOrganisationRemoved;
import uk.gov.moj.cpp.defence.events.CaseAssignedToAdvocate;
import uk.gov.moj.cpp.defence.events.CaseAssignedToOrganisation;
import uk.gov.moj.cpp.defence.events.CaseAssignmentToAdvocateRemoved;
import uk.gov.moj.cpp.defence.events.CasesAssignedToAdvocate;
import uk.gov.moj.cpp.defence.events.CasesAssignedToOrganisation;
import uk.gov.moj.defence.domain.common.pojo.CaseHearingAssignmentDetails;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("pmd:NullAssignment")
public class Advocate implements Aggregate {
    private static final Logger LOGGER = LoggerFactory.getLogger(Advocate.class.getName());
    private static final long serialVersionUID = -7950876778016335076L;
    private static final String DEFENCE_LAWYER = "Defence Lawyers";
    private final Set<UUID> assignedCases = new HashSet<>();
    private UUID assigneeOrganisationId;

    public Stream<Object> assignCase(final String assigneeEmail, final PersonDetails assigneeDetails, final Organisation assigneeOrganisation,
                                     final Organisation assignorOrganisation, final PersonDetails assignorDetails, final UUID caseId,
                                     final List<String> assigneeGroupList, final boolean isAssigneeDefending, final UUID prosecutingAuthorityId,
                                     final boolean isCps, final boolean isPolice, final String representingOrganisation) {

        if (assigneeDetails == null) {
            return apply(of(UserNotFound.userNotFound()
                    .withEmail(assigneeEmail)
                    .withFailureReason(USER_NOT_FOUND.getMessage())
                    .withErrorCode(parseInt(USER_NOT_FOUND.getCode()))
                    .build()));
        }

        if (assigneeOrganisation == null || !isAssigneeInAllowedGroupsToGetCaseAccess(assigneeGroupList)) {
            return apply(of(AssigneeNotInAllowedGroups.assigneeNotInAllowedGroups()
                    .withEmail(assigneeEmail)
                    .withFailureReason(ASSIGNEE_NOT_IN_ALLOWED_GROUPS.getMessage())
                    .withErrorCode(parseInt(ASSIGNEE_NOT_IN_ALLOWED_GROUPS.getCode()))
                    .build()));
        }

        if (isAssigneeDefending) {
            return apply(of(AssigneeForProsecutionIsDefendingCase.assigneeForProsecutionIsDefendingCase()
                    .withEmail(assigneeEmail)
                    .withFailureReason(ASSIGNEE_DEFENDING_CASE.getMessage())
                    .withErrorCode(parseInt(ASSIGNEE_DEFENDING_CASE.getCode()))
                    .build()));
        }

        final Stream.Builder<Object> builder = builder();
        if (isAlreadyAssigned(caseId)) {
            builder.add(UserAlreadyAssigned.userAlreadyAssigned()
                    .withEmail(assigneeEmail)
                    .withFailureReason(ALREADY_ASSIGNED.getMessage())
                    .withErrorCode(parseInt(ALREADY_ASSIGNED.getCode()))
                    .build());
        } else {
            final boolean isAssigneeAnOrganisation = assigneeGroupList.contains(DEFENCE_LAWYER);
            if (isAssigneeAnOrganisation) {
                builder.add(buildCaseAssignedToOrganisation(assigneeDetails, assigneeOrganisation, assignorOrganisation, assignorDetails, caseId, prosecutingAuthorityId, isCps, isPolice, representingOrganisation));
            } else {
                builder.add(buildCaseAssignedToAdvocate(assigneeDetails, assigneeOrganisation, assignorOrganisation, assignorDetails, caseId, prosecutingAuthorityId, isCps, isPolice, representingOrganisation));
            }
        }

        return apply(builder.build());

    }

    public Stream<Object> removeCaseAssignment(UUID caseId, UUID assigneeUserId, List<String> assigneeGroupList,
                                               boolean hasAdvocatesAssignedToTheCase, UUID removedByUserId, final boolean isAutomaticUnassignment) {

        if (!isAlreadyAssigned(caseId)) {
            if (!isAutomaticUnassignment) {
                return apply(of(UserNotAssigned.userNotAssigned()
                        .withAssigneeUserId(assigneeUserId)
                        .withFailureReason(USER_NOT_ASSIGNED.getMessage())
                        .withErrorCode(parseInt(USER_NOT_ASSIGNED.getCode()))
                        .build()));
            } else {
                LOGGER.warn("User not assigned for caseId: {} userId: {}", caseId, assigneeUserId);
                return empty();
            }
        }

        final boolean isAssigneeAnOrganisation = assigneeGroupList.contains(DEFENCE_LAWYER);
        if (isAssigneeAnOrganisation) {
            if (hasAdvocatesAssignedToTheCase) {
                LOGGER.error("RemoveCaseAssignmentToOrganisationFailed(Organisation has other advocate assignments to the case) - " +
                                "caseId:{}, assigneeOrganisationId:{}, assigneeUserId:{}, isAutomaticUnassignment:{}",
                        caseId, this.getAssigneeOrganisationId(), assigneeUserId, isAutomaticUnassignment);
                return empty();
            } else {
                return apply(of(CaseAssigmentToOrganisationRemoved.caseAssigmentToOrganisationRemoved()
                        .withCaseId(caseId)
                        .withAssigneeOrganisationId(this.assigneeOrganisationId)
                        .withRemovedByUserId(removedByUserId)
                        .withRemovedTimestamp(now())
                        .withIsAutomaticUnassignment(isAutomaticUnassignment)
                        .build()));
            }
        } else {
            return apply(of(CaseAssignmentToAdvocateRemoved.caseAssignmentToAdvocateRemoved()
                    .withCaseId(caseId)
                    .withAssigneeUserId(assigneeUserId)
                    .withAssigneeOrganisationId(this.assigneeOrganisationId)
                    .withRemovedByUserId(removedByUserId)
                    .withRemovedTimestamp(now())
                    .withIsAutomaticUnassignment(isAutomaticUnassignment)
                    .build()));
        }
    }

    public boolean isAlreadyAssigned(final UUID caseId) {
        return this.assignedCases.contains(caseId);
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(CaseAssignedToAdvocate.class)
                        .apply(caseAssignedToAdvocate -> {
                                    this.assignedCases.add(caseAssignedToAdvocate.getCaseId());
                                    this.assigneeOrganisationId = caseAssignedToAdvocate.getAssigneeOrganisation().getOrgId();
                                }
                        ),
                when(CaseAssignedToOrganisation.class)
                        .apply(caseAssignedToOrganisation -> {
                                    this.assignedCases.add(caseAssignedToOrganisation.getCaseId());
                                    this.assigneeOrganisationId = caseAssignedToOrganisation.getAssigneeOrganisation().getOrgId();
                                }
                        ),
                when(CaseAssigmentToOrganisationRemoved.class)
                        .apply(caseAssigmentToOrganisationRemoved -> this.assignedCases.remove(caseAssigmentToOrganisationRemoved.getCaseId())
                        ),
                when(CaseAssignmentToAdvocateRemoved.class)
                        .apply(caseAssignmentToAdvocateRemoved -> this.assignedCases.remove(caseAssignmentToAdvocateRemoved.getCaseId())
                        ),
                when(CasesAssignedToAdvocate.class)
                        .apply(caseAssignedToAdvocate -> {
                                    caseAssignedToAdvocate.getCaseHearingAssignments().forEach(ch -> this.assignedCases.add(ch.getCaseId()));
                                    this.assigneeOrganisationId = caseAssignedToAdvocate.getAssigneeOrganisation().getOrgId();
                                }
                        ),
                when(CasesAssignedToOrganisation.class)
                        .apply(caseAssignedToOrganisation -> {
                                    caseAssignedToOrganisation.getCaseHearingAssignments().forEach(ch -> this.assignedCases.add(ch.getCaseId()));
                                    this.assigneeOrganisationId = caseAssignedToOrganisation.getAssigneeOrganisation().getOrgId();
                                }
                        ),
                otherwiseDoNothing()
        );
    }

    public UUID getAssigneeOrganisationId() {
        return assigneeOrganisationId;
    }

    public Stream<Object> assignCaseHearing(final String assigneeEmail, final PersonDetails assigneeDetails, final Organisation assigneeOrganisation,
                                            final List<String> assigneeGroupList, final PersonDetails assignorDetails, final Organisation assignorOrganisation,
                                            final List<CaseHearingAssignmentDetails> caseHearingAssignmentDetails, final String representingOrganisation) {

        final List<AssignmentError> caseAssignmentsByHearingValidationErrors = verifyForCaseAssignmentErrors(assigneeDetails, assigneeOrganisation, assigneeGroupList, caseHearingAssignmentDetails);
        if (!caseAssignmentsByHearingValidationErrors.isEmpty()) {
            final CaseAssignmentsByHearingListingFailed.Builder assignmentFailedEventBuilder = caseAssignmentsByHearingListingFailed()
                    .withEmail(assigneeEmail)
                    .withAssignmentErrors(caseAssignmentsByHearingValidationErrors);
            Optional.ofNullable(assigneeDetails).ifPresent(ad -> assignmentFailedEventBuilder.withAssigneeUserId(ad.getUserId()));

            return apply(of(assignmentFailedEventBuilder.build()));
        }

        final boolean isAssigneeAnOrganisation = assigneeGroupList.contains(DEFENCE_LAWYER);
        final Stream.Builder<Object> streamBuilder = Stream.builder();
        if (isAssigneeAnOrganisation) {
            streamBuilder.add(buildCasesAssignedToOrganisation(assigneeDetails, assigneeOrganisation, assignorOrganisation, assignorDetails, caseHearingAssignmentDetails, representingOrganisation));
        } else {
            streamBuilder.add(buildCasesAssignedToAdvocate(assigneeDetails, assigneeOrganisation, assignorOrganisation, assignorDetails, caseHearingAssignmentDetails, representingOrganisation));
        }

        return apply(streamBuilder.build());
    }

    private List<AssignmentError> verifyForCaseAssignmentErrors(final PersonDetails assigneeDetails, final Organisation assigneeOrganisation,
                                                                final List<String> assigneeGroupList, final List<CaseHearingAssignmentDetails> caseHearingAssignmentDetails) {

        return caseHearingAssignmentDetails.stream()
                .map(caseHearingAssignmentDetail -> {
                    if (nonNull(caseHearingAssignmentDetail.getFailureReason()) && nonNull(caseHearingAssignmentDetail.getErrorCode())) {
                        return toAssignmentError(caseHearingAssignmentDetail, valueOf(caseHearingAssignmentDetail.getFailureReason()));
                    }
                    return validateCaseAssignment(assigneeDetails, assigneeOrganisation, assigneeGroupList,
                            caseHearingAssignmentDetail.getAssigneeDefendingCase())
                            .map(type -> toAssignmentError(caseHearingAssignmentDetail, type))
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Optional<ErrorType> validateCaseAssignment(final PersonDetails assigneeDetails, final Organisation assigneeOrganisation,
                                                       final List<String> assigneeGroupList, final Boolean isAssigneeDefending) {
        if (isNull(assigneeDetails)) {
            return Optional.of(USER_NOT_FOUND);
        }
        if (assigneeOrganisation == null || !isAssigneeInAllowedGroupsToGetCaseAccess(assigneeGroupList)) {
            return Optional.of(ASSIGNEE_NOT_IN_ALLOWED_GROUPS);
        }
        if (isAssigneeDefending) {
            return Optional.of(ASSIGNEE_DEFENDING_CASE);
        }
        return Optional.empty();
    }
}
