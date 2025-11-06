package uk.gov.moj.cpp.defence.persistence;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.justice.services.test.utils.persistence.BaseTransactionalJunit4Test;
import uk.gov.moj.cpp.defence.persistence.entity.AssignmentUserDetails;
import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionAdvocateAccess;
import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionOrganisationAccess;
import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionOrganisationCaseKey;
import uk.gov.moj.cpp.defence.persistence.entity.RepresentationType;


import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(CdiTestRunner.class)
public class AdvocateAccessRepositoryTest extends BaseTransactionalJunit4Test {

    @Inject
    AdvocateAccessRepository advocateAccessRepository;

    @Inject
    OrganisationAccessRepository organisationAccessRepository;

    private static final UUID CASE_ID = randomUUID();
    private static final UUID ASSIGNEE_ORG_ID = randomUUID();
    private static final UUID ASSIGNEE_ID = randomUUID();
    private static final UUID ASSIGNOR_ID = randomUUID();
    private static final ZonedDateTime ASSIGNED_TIME = now();
    private static final ZonedDateTime ASSIGNMENT_EXPIRED_DATE = now().plusDays(5);

    @Test
    public void shouldFindCaseIdAndAssigneeId() {

        final List<ProsecutionAdvocateAccess> prosecutionAdvocateAccessList = buildProsecutionAdvocateAccessEntity();

        final List<ProsecutionAdvocateAccess> advocateAccessesListFromDb = advocateAccessRepository.findByCaseIdAndAssigneeId(CASE_ID, ASSIGNEE_ID);

        assertThat(advocateAccessesListFromDb.get(0).getCaseId(), is(prosecutionAdvocateAccessList.get(0).getCaseId()));
        assertThat(advocateAccessesListFromDb.get(0).getAssigneeDetails(), is(prosecutionAdvocateAccessList.get(0).getAssigneeDetails()));
        assertThat(advocateAccessesListFromDb.get(0).getAssignmentExpiryDate(), is(prosecutionAdvocateAccessList.get(0).getAssignmentExpiryDate()));
        assertThat(advocateAccessesListFromDb.get(0).getAssignedDate(), is(prosecutionAdvocateAccessList.get(0).getAssignedDate()));
        assertThat(advocateAccessesListFromDb.get(0).getProsecutionOrganisation(), is(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation()));
    }


    @Test
    public void whenPersistEntitiesWithOneToManyAssociation_thenSuccess() {
        ProsecutionOrganisationAccess prosecutionOrganisationAccess = new ProsecutionOrganisationAccess();
        final UUID caseId = randomUUID();
        final UUID assigneeOrgId = randomUUID();
        final UUID assigneeId = randomUUID();
        final UUID assigneeUserId = randomUUID();
        prosecutionOrganisationAccess.setId(new ProsecutionOrganisationCaseKey(caseId, assigneeOrgId));
        prosecutionOrganisationAccess.setAssigneeDetails(new AssignmentUserDetails(assigneeId, assigneeUserId, "f", "l"));
        prosecutionOrganisationAccess.setAssignedDate(now());
        prosecutionOrganisationAccess.setCaseId(caseId);
        prosecutionOrganisationAccess.setRepresentationType(RepresentationType.PROSECUTION);
        prosecutionOrganisationAccess.setRepresenting("CPS");
        prosecutionOrganisationAccess.setAssignorDetails(new AssignmentUserDetails(randomUUID(), randomUUID(), "f", "l"));
        ProsecutionAdvocateAccess prosecutionAdvocateAccess = new ProsecutionAdvocateAccess();
        prosecutionAdvocateAccess.setId(randomUUID());
        prosecutionAdvocateAccess.setCaseId(caseId);
        prosecutionAdvocateAccess.setAssignedDate(now());
        prosecutionAdvocateAccess.setProsecutionOrganisation(prosecutionOrganisationAccess);
        prosecutionOrganisationAccess.getProsecutionAdvocatesWithAccess().add(prosecutionAdvocateAccess);
        assertThat(organisationAccessRepository.findByAssigneeOrganisationIdAndCaseId(assigneeOrgId, caseId), is(Optional.empty()));

        organisationAccessRepository.save(prosecutionOrganisationAccess);
        advocateAccessRepository.save(prosecutionAdvocateAccess);

        List<ProsecutionAdvocateAccess>  prosecutionAdvocateAccessList = advocateAccessRepository.findAll();

        assertThat(prosecutionAdvocateAccessList.size(), Is.is(1));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getAssigneeDetails().getUserId(), Is.is(assigneeUserId));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getAssigneeDetails().getId(), Is.is(assigneeId));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getAssigneeDetails().getFirstName(), Is.is("f"));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getAssigneeDetails().getLastName(), Is.is("l"));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getAssignedDate(), Is.is(prosecutionOrganisationAccess.getAssignedDate()));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getCaseId(), Is.is(caseId));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getRepresentationType(), Is.is(RepresentationType.PROSECUTION));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getRepresenting(), Is.is("CPS"));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getAssignorDetails().getUserId(), Is.is(prosecutionOrganisationAccess.getAssignorDetails().getUserId()));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getAssignorDetails().getId(), Is.is(prosecutionOrganisationAccess.getAssignorDetails().getId()));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getAssignorDetails().getFirstName(), Is.is(prosecutionOrganisationAccess.getAssignorDetails().getFirstName()));
        assertThat(prosecutionAdvocateAccessList.get(0).getProsecutionOrganisation().getAssignorDetails().getLastName(), Is.is(prosecutionOrganisationAccess.getAssignorDetails().getLastName()));
        assertThat(prosecutionAdvocateAccessList.get(0).getCaseId(),is(caseId));

        Optional<ProsecutionOrganisationAccess> prosecutionOrganisationAccessResult = organisationAccessRepository.findByAssigneeOrganisationIdAndCaseId(assigneeOrgId, caseId);
        assertThat(prosecutionOrganisationAccessResult.isPresent(), is(true));
        assertThat(prosecutionOrganisationAccessResult.get(), is(prosecutionOrganisationAccess));
    }

    @Test
    public void shouldGetExpiredProsecutorAssignmentsWithLimitWhenProsecutionOrganisationAccessDoesNotExistForCaseIdAndOrganisationId() {

        final UUID assigneeUserId = randomUUID();
        final UUID assigneeId = randomUUID();
        final UUID caseId = randomUUID();
        final UUID caseId2 = randomUUID();
        final UUID assignorId = randomUUID();
        final UUID assignorUserId = randomUUID();
        final UUID assignorOrganisationId = randomUUID();
        final UUID assigneeOrgId = randomUUID();

        ProsecutionAdvocateAccess prosecutionAdvocateAccess = new ProsecutionAdvocateAccess();
        prosecutionAdvocateAccess.setId(randomUUID());
        prosecutionAdvocateAccess.setCaseId(caseId);
        prosecutionAdvocateAccess.setAssignedDate(now());
        prosecutionAdvocateAccess.setAssignorOrganisationId(assignorOrganisationId);
        prosecutionAdvocateAccess.setAssignorDetails(new AssignmentUserDetails(assignorId, assignorUserId, "f", "l"));
        prosecutionAdvocateAccess.setAssigneeDetails(new AssignmentUserDetails(assigneeId, assigneeUserId, "m", "t"));
        prosecutionAdvocateAccess.setAssignmentExpiryDate(now().minusDays(1));
        advocateAccessRepository.save(prosecutionAdvocateAccess);

        ProsecutionOrganisationAccess prosecutionOrganisationAccess = new ProsecutionOrganisationAccess();

        prosecutionOrganisationAccess.setId(new ProsecutionOrganisationCaseKey(caseId, assigneeOrgId));
        prosecutionOrganisationAccess.setAssigneeDetails(new AssignmentUserDetails(randomUUID(), randomUUID(), "f", "l"));
        prosecutionOrganisationAccess.setAssignedDate(now());
        prosecutionOrganisationAccess.setCaseId(caseId);
        prosecutionOrganisationAccess.setRepresentationType(RepresentationType.PROSECUTION);
        prosecutionOrganisationAccess.setRepresenting("CPS");
        prosecutionOrganisationAccess.setAssignorDetails(new AssignmentUserDetails(randomUUID(), randomUUID(), "f", "l"));
        ProsecutionAdvocateAccess prosecutionAdvocateAccess2 = new ProsecutionAdvocateAccess();
        prosecutionAdvocateAccess2.setId(randomUUID());
        prosecutionAdvocateAccess2.setCaseId(caseId2);
        prosecutionAdvocateAccess2.setAssignedDate(now());
        prosecutionAdvocateAccess2.setAssignmentExpiryDate(now().minusDays(2));
        prosecutionAdvocateAccess2.setProsecutionOrganisation(prosecutionOrganisationAccess);
        prosecutionOrganisationAccess.getProsecutionAdvocatesWithAccess().add(prosecutionAdvocateAccess);
        organisationAccessRepository.save(prosecutionOrganisationAccess);
        advocateAccessRepository.save(prosecutionAdvocateAccess2);

        final List<ProsecutionAdvocateAccess> prosecutionAdvocateAccessList = advocateAccessRepository.findExpiredCaseAssignments(50);
        assertThat(prosecutionAdvocateAccessList.size(), Is.is(1));


    }

    private List<ProsecutionAdvocateAccess> buildProsecutionAdvocateAccessEntity() {
        final List<ProsecutionAdvocateAccess> advocateAccessList = new ArrayList<>();

        final AssignmentUserDetails assignmentUser = new AssignmentUserDetails();
        assignmentUser.setUserId(ASSIGNEE_ID);
        assignmentUser.setId(randomUUID());
        assignmentUser.setFirstName("First name");
        assignmentUser.setLastName("Last name");

        final AssignmentUserDetails assignorUser = new AssignmentUserDetails();
        assignorUser.setId(randomUUID());
        assignorUser.setFirstName("assignor");
        assignorUser.setLastName("assignor lastname");
        assignorUser.setUserId(ASSIGNOR_ID);

        final ProsecutionOrganisationAccess prosecutionOrganisation = buildProsecutionOrganisationAccess();

        organisationAccessRepository.saveAndFlush(prosecutionOrganisation);

        final ProsecutionAdvocateAccess advocateAccess = new ProsecutionAdvocateAccess();
        advocateAccess.setId(randomUUID());
        advocateAccess.setCaseId(CASE_ID);
        advocateAccess.setAssignorOrganisationId(ASSIGNEE_ORG_ID);
        advocateAccess.setAssigneeDetails(assignmentUser);
        advocateAccess.setAssignmentExpiryDate(now().plusMonths(1));
        advocateAccess.setAssignedDate(ASSIGNED_TIME);
        advocateAccess.setAssignorDetails(assignorUser);
        advocateAccess.setAssignorOrganisationName("CPS");
        advocateAccess.setProsecutionOrganisation(prosecutionOrganisation);
        advocateAccessRepository.save(advocateAccess);
        advocateAccessList.add(advocateAccess);

        return advocateAccessList;
    }

    private static ProsecutionOrganisationAccess buildProsecutionOrganisationAccess() {
        final ProsecutionOrganisationCaseKey caseKey = new ProsecutionOrganisationCaseKey();
        caseKey.setAssigneeOrganisationId(ASSIGNEE_ORG_ID);
        caseKey.setCaseId(CASE_ID);
        ProsecutionOrganisationAccess prosecutionOrganisation = new ProsecutionOrganisationAccess();
        prosecutionOrganisation.setId(caseKey);
        prosecutionOrganisation.setCaseId(CASE_ID);
        prosecutionOrganisation.setAssignedDate(ASSIGNED_TIME);
        prosecutionOrganisation.setAssignmentExpiryDate(ASSIGNMENT_EXPIRED_DATE);
        prosecutionOrganisation.setRepresentationType(RepresentationType.PROSECUTION);
        prosecutionOrganisation.setRepresenting("CPS");

        return prosecutionOrganisation;
    }

}