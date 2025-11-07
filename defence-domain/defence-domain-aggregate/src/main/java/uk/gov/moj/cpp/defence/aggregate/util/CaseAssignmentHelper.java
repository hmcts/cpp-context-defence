package uk.gov.moj.cpp.defence.aggregate.util;

import static java.lang.Integer.parseInt;
import static java.time.ZonedDateTime.now;

import uk.gov.justice.cps.defence.AssignmentError;
import uk.gov.justice.cps.defence.PersonDetails;
import uk.gov.moj.cpp.defence.Organisation;
import uk.gov.moj.cpp.defence.common.util.ErrorType;
import uk.gov.moj.cpp.defence.events.CaseAssignedToAdvocate;
import uk.gov.moj.cpp.defence.events.CaseAssignedToOrganisation;
import uk.gov.moj.cpp.defence.events.CaseHearingAssignments;
import uk.gov.moj.cpp.defence.events.CasesAssignedToAdvocate;
import uk.gov.moj.cpp.defence.events.CasesAssignedToOrganisation;
import uk.gov.moj.defence.domain.common.pojo.CaseHearingAssignmentDetails;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CaseAssignmentHelper {

    private CaseAssignmentHelper(){
        //do nothing
    }

    public static CaseAssignedToOrganisation buildCaseAssignedToOrganisation(final PersonDetails assigneeDetails, final Organisation assigneeOrganisation, final Organisation assignorOrganisation, final PersonDetails assignorDetails, UUID caseId, final UUID prosecutingAuthorityId, final boolean isCps, final boolean isPolice, final String representingOrganisation) {
        return CaseAssignedToOrganisation.caseAssignedToOrganisation()
                .withAssigneeDetails(assigneeDetails)
                .withAssigneeOrganisation(assigneeOrganisation)
                .withAssignmentTimestamp(now())
                .withAssignorDetails(assignorDetails)
                .withAssignorOrganisation(assignorOrganisation)
                .withRepresentingOrganisation(representingOrganisation)
                .withProsecutingAuthorityId(prosecutingAuthorityId)
                .withCaseId(caseId)
                .withIsCps(isCps)
                .withIsPolice(isPolice)
                .build();
    }

    public static CaseAssignedToAdvocate buildCaseAssignedToAdvocate(final PersonDetails assigneeDetails, final Organisation assigneeOrganisation, final Organisation assignorOrganisation, final PersonDetails assignorDetails, UUID caseId, final UUID prosecutingAuthorityId, final boolean isCps, final boolean isPolice, final String representingOrganisation) {
        return CaseAssignedToAdvocate.caseAssignedToAdvocate()
                .withAssigneeDetails(assigneeDetails)
                .withAssigneeOrganisation(assigneeOrganisation)
                .withAssignmentTimestamp(now())
                .withAssignorDetails(assignorDetails)
                .withAssignorOrganisation(assignorOrganisation)
                .withRepresentingOrganisation(representingOrganisation)
                .withCaseId(caseId)
                .withIsCps(isCps)
                .withIsPolice(isPolice)
                .withProsecutingAuthorityId(prosecutingAuthorityId)
                .build();
    }

    public static CasesAssignedToAdvocate buildCasesAssignedToAdvocate(final PersonDetails assigneeDetails, final Organisation assigneeOrganisation, final Organisation assignorOrganisation,
                                                                       final PersonDetails assignorDetails, final List<CaseHearingAssignmentDetails> caseHearingAssignmentDetails, final String representingOrganisation) {

        final List<CaseHearingAssignments> caseHearingAssignments = caseHearingAssignmentDetails.stream()
                .map(CaseAssignmentHelper::toCaseHearingAssignment)
                .collect(Collectors.toList());

        return CasesAssignedToAdvocate.casesAssignedToAdvocate()
                .withAssigneeDetails(assigneeDetails)
                .withAssigneeOrganisation(assigneeOrganisation)
                .withAssignmentTimestamp(now())
                .withAssignorDetails(assignorDetails)
                .withAssignorOrganisation(assignorOrganisation)
                .withRepresentingOrganisation(representingOrganisation)
                .withCaseHearingAssignments(caseHearingAssignments)
                .build();
    }

    public static CasesAssignedToOrganisation buildCasesAssignedToOrganisation(final PersonDetails assigneeDetails, final Organisation assigneeOrganisation, final Organisation assignorOrganisation,
                                                                               final PersonDetails assignorDetails, final List<CaseHearingAssignmentDetails> caseHearingAssignmentDetails, final String representingOrganisation) {

        final List<CaseHearingAssignments> caseHearingAssignments = caseHearingAssignmentDetails.stream()
                .map(CaseAssignmentHelper::toCaseHearingAssignment).collect(Collectors.toList());

        return CasesAssignedToOrganisation.casesAssignedToOrganisation()
                .withAssigneeDetails(assigneeDetails)
                .withAssigneeOrganisation(assigneeOrganisation)
                .withAssignmentTimestamp(now())
                .withAssignorDetails(assignorDetails)
                .withAssignorOrganisation(assignorOrganisation)
                .withRepresentingOrganisation(representingOrganisation)
                .withCaseHearingAssignments(caseHearingAssignments)
                .build();
    }

    public static AssignmentError toAssignmentError(CaseHearingAssignmentDetails caseHearingAssignmentDetail, ErrorType type) {
        return new AssignmentError.Builder()
                .withCaseId(caseHearingAssignmentDetail.getCaseId())
                .withHearingId(caseHearingAssignmentDetail.getHearingId())
                .withErrorCode(parseInt(type.getCode()))
                .withFailureReason(type.name())
                .build();
    }

    private static CaseHearingAssignments toCaseHearingAssignment(CaseHearingAssignmentDetails caseHearingAssignmentDetails) {
        return CaseHearingAssignments.caseHearingAssignments()
                .withCaseId(caseHearingAssignmentDetails.getCaseId())
                .withHearingId(caseHearingAssignmentDetails.getHearingId())
                .withIsCps(caseHearingAssignmentDetails.getCps())
                .withIsPolice(caseHearingAssignmentDetails.getPolice())
                .withProsecutingAuthorityId(caseHearingAssignmentDetails.getProsecutionAuthorityId())
                .build();
    }
}
