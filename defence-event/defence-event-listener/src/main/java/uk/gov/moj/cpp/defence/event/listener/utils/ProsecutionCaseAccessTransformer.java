package uk.gov.moj.cpp.defence.event.listener.utils;

import static java.util.UUID.randomUUID;

import uk.gov.justice.cps.defence.PersonDetails;
import uk.gov.moj.cpp.defence.Organisation;
import uk.gov.moj.cpp.defence.persistence.entity.AssignmentUserDetails;
import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionAdvocateAccess;
import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionOrganisationAccess;
import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionOrganisationCaseKey;
import uk.gov.moj.cpp.defence.persistence.entity.RepresentationType;

import java.time.ZonedDateTime;
import java.util.UUID;

public class ProsecutionCaseAccessTransformer {

    private ProsecutionCaseAccessTransformer() {
        //do nothing
    }

    public static ProsecutionOrganisationAccess toOrganisationAccess(final UUID caseId, final Organisation assigneeOrganisation,
                                                                     final PersonDetails assigneeUserDetails, final PersonDetails assignorUserDetails,
                                                                     final Organisation assignorOrganisation, final String representingOrganisation,
                                                                     final ZonedDateTime assignmentTimestamp) {

        final ProsecutionOrganisationAccess organisationAccess = new ProsecutionOrganisationAccess();
        organisationAccess.setId(new ProsecutionOrganisationCaseKey(caseId, assigneeOrganisation.getOrgId()));

        final AssignmentUserDetails assignorDetails = new AssignmentUserDetails(randomUUID(), assignorUserDetails.getUserId(), assignorUserDetails.getFirstName(), assignorUserDetails.getLastName());
        organisationAccess.setAssignorDetails(assignorDetails);
        organisationAccess.setAssignorOrganisationId(assignorOrganisation.getOrgId());
        organisationAccess.setAssignorOrganisationName(assignorOrganisation.getOrganisationName());

        final AssignmentUserDetails assigneeDetails = new AssignmentUserDetails(randomUUID(), assigneeUserDetails.getUserId(), assigneeUserDetails.getFirstName(), assigneeUserDetails.getLastName());
        organisationAccess.setAssigneeDetails(assigneeDetails);
        organisationAccess.setAssigneeOrganisationName(assigneeOrganisation.getOrganisationName());

        organisationAccess.setRepresentationType(RepresentationType.PROSECUTION);
        organisationAccess.setRepresenting(representingOrganisation);
        organisationAccess.setAssignedDate(assignmentTimestamp);

        return organisationAccess;
    }

    public static ProsecutionAdvocateAccess toAdvocateAccess(final PersonDetails assigneeUserDetails, final PersonDetails assignorUserDetails,
                                                             final Organisation assignorOrganisation, final ProsecutionOrganisationAccess assigneeOrganisation,
                                                             final ZonedDateTime assignmentTimestamp) {
        final ProsecutionAdvocateAccess advocateAccess = new ProsecutionAdvocateAccess();
        advocateAccess.setId(randomUUID());

        advocateAccess.setProsecutionOrganisation(assigneeOrganisation);
        final AssignmentUserDetails assigneeDetails = new AssignmentUserDetails(randomUUID(), assigneeUserDetails.getUserId(), assigneeUserDetails.getFirstName(), assigneeUserDetails.getLastName());
        advocateAccess.setAssigneeDetails(assigneeDetails);

        final AssignmentUserDetails assignorDetails = new AssignmentUserDetails(randomUUID(), assignorUserDetails.getUserId(), assignorUserDetails.getFirstName(), assignorUserDetails.getLastName());
        advocateAccess.setAssignorDetails(assignorDetails);

        advocateAccess.setAssignorOrganisationId(assignorOrganisation.getOrgId());
        advocateAccess.setAssignorOrganisationName(assignorOrganisation.getOrganisationName());

        advocateAccess.setAssignedDate(assignmentTimestamp);
        return advocateAccess;
    }


    public static void updateAssignorDetailsAndExpiryDate(final ProsecutionOrganisationAccess existingEntity, final PersonDetails assignorPersonalDetails,
                                                          final Organisation assignorOrganisation, final ZonedDateTime assignmentTimestamp, final int expiryHours) {

        if (!existingEntity.getAssignorDetails().getUserId().equals(assignorPersonalDetails.getUserId())) {
            final AssignmentUserDetails assignorDetails = new AssignmentUserDetails(randomUUID(), assignorPersonalDetails.getUserId(),
                    assignorPersonalDetails.getFirstName(), assignorPersonalDetails.getLastName());
            existingEntity.setAssignorDetails(assignorDetails);
            existingEntity.setAssignorOrganisationId(assignorOrganisation.getOrgId());
            existingEntity.setAssignorOrganisationName(assignorOrganisation.getOrganisationName());
        }

        existingEntity.setAssignedDate(assignmentTimestamp);
        existingEntity.setAssignmentExpiryDate(assignmentTimestamp.plusHours(expiryHours));
    }

}
