package uk.gov.moj.cpp.defence.aggregate;

import static com.google.common.collect.ImmutableList.of;
import static java.lang.Integer.parseInt;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.cps.defence.AssigneeForProsecutionIsDefendingCase;
import uk.gov.justice.cps.defence.AssigneeNotInAllowedGroups;
import uk.gov.justice.cps.defence.CaseAssignmentsByHearingListingFailed;
import uk.gov.justice.cps.defence.PersonDetails;
import uk.gov.justice.cps.defence.UserAlreadyAssigned;
import uk.gov.justice.cps.defence.UserNotAssigned;
import uk.gov.justice.cps.defence.UserNotFound;
import uk.gov.moj.cpp.defence.Organisation;
import uk.gov.moj.cpp.defence.common.util.ErrorType;
import uk.gov.moj.cpp.defence.events.CaseAssigmentToOrganisationRemoved;
import uk.gov.moj.cpp.defence.events.CaseAssignedToAdvocate;
import uk.gov.moj.cpp.defence.events.CaseAssignedToOrganisation;
import uk.gov.moj.cpp.defence.events.CaseAssignmentToAdvocateRemoved;
import uk.gov.moj.cpp.defence.events.CasesAssignedToAdvocate;
import uk.gov.moj.cpp.defence.events.CasesAssignedToOrganisation;
import uk.gov.moj.defence.domain.common.pojo.CaseHearingAssignmentDetails;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AdvocateTest {

    public static final String ASSIGNEE_NAME = "assigneeName";
    public static final String ASSIGNEE_LAST_NAME = "assigneeLastName";
    public static final String ASSIGNER_NAME = "assignerName";
    public static final String ASSIGNER_LAST_NAME = "assignerLastName";
    public static final String DEFENCE_LAWYERS = "Defence Lawyers";
    public static final String ADVOCATES_ROLE = "Advocates";
    @InjectMocks
    private Advocate advocate;

    private final UUID caseId = randomUUID();
    private final UUID hearingId = randomUUID();
    private final UUID prosecutionAuthId = randomUUID();

    @Test
    public void shouldRaiseUserNotFoundWhenAssigneeDetailsIsNotAvailable() {

        final String assigneeEmail = "not-exist-email@hmcts.net";
        final UUID caseId = randomUUID();

        final Stream<Object> eventStream = advocate.assignCase(assigneeEmail, null, null, null, null, caseId, null, false, randomUUID(), true, true, "CPS");

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(UserNotFound.class.getName()));

        final UserNotFound userNotFoundEvent = (UserNotFound) eventList.get(0);
        assertThat(userNotFoundEvent.getEmail(), is(assigneeEmail));
    }

    @Test
    public void shouldRaiseAssigneeUserNotInAllowedGroupsWhenAssigneeIsNotDefenceLawyerOrAdvocate() {

        final String assigneeEmail = "email@hmcts.net";
        final UUID assigneeUserId = UUID.randomUUID();
        final PersonDetails assigneePersonDetails = PersonDetails.personDetails()
                .withFirstName("assignee_name")
                .withLastName("assignee_lastname")
                .withUserId(assigneeUserId)
                .build();

        final List<String> assigneeGroupList = of("System Users");

        final UUID caseId = randomUUID();
        final Stream<Object> eventStream = advocate.assignCase(assigneeEmail, assigneePersonDetails, null, null, null, caseId, assigneeGroupList, false, randomUUID(), true, true, "CPS");

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(AssigneeNotInAllowedGroups.class.getName()));

        final AssigneeNotInAllowedGroups assigneeNotInAllowedGroups = (AssigneeNotInAllowedGroups) eventList.get(0);
        assertThat(assigneeNotInAllowedGroups.getEmail(), is(assigneeEmail));
    }

    @Test
    public void shouldRaiseUserAlreadyAssignedWhenCaseIsAlreadyAssigned() {

        final String assigneeEmail = "email@hmcts.net";
        final UUID assigneeUserId = UUID.randomUUID();


        final PersonDetails assigneePersonDetails = PersonDetails.personDetails()
                .withFirstName("assignee_name")
                .withLastName("assignee_lastname")
                .withUserId(assigneeUserId)
                .build();


        final List<String> assigneeGroupList = of(DEFENCE_LAWYERS);
        final Organisation assigneeOrganisation = Organisation.organisation().withOrgId(UUID.randomUUID()).build();
        final Organisation assignerOrganisation = Organisation.organisation().withOrgId(randomUUID()).build();
        final UUID caseId = randomUUID();

        advocate.assignCase(assigneeEmail, assigneePersonDetails, assigneeOrganisation, assignerOrganisation, null, caseId, assigneeGroupList, false, randomUUID(), true, true, "CPS");
        final Stream<Object> eventStream = advocate.assignCase(assigneeEmail, assigneePersonDetails, assigneeOrganisation, assignerOrganisation, null, caseId, assigneeGroupList, false, randomUUID(), true, true, "CPS");
        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(UserAlreadyAssigned.class.getName()));

        final UserAlreadyAssigned userAlreadyAssigned = (UserAlreadyAssigned) eventList.get(0);
        assertThat(userAlreadyAssigned.getEmail(), is(assigneeEmail));
    }

    @Test
    public void shouldRaiseAssigneeIsDefendingTheCaseEventWhenAssigneeIsDefending() {

        final String assigneeEmail = "not-exist-email@hmcts.net";
        final UUID caseId = randomUUID();

        final PersonDetails assigneePersonDetails = PersonDetails.personDetails()
                .withFirstName(ASSIGNEE_NAME)
                .withLastName(ASSIGNEE_LAST_NAME)
                .withUserId(randomUUID())
                .build();

        final Stream<Object> eventStream = advocate.assignCase(assigneeEmail, assigneePersonDetails, Organisation.organisation().withOrgId(randomUUID()).build(), null, null, caseId, of(DEFENCE_LAWYERS), true, randomUUID(), true, true, "CPS");

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(AssigneeForProsecutionIsDefendingCase.class.getName()));

        final AssigneeForProsecutionIsDefendingCase assigneeForProsecutionIsDefendingCase = (AssigneeForProsecutionIsDefendingCase) eventList.get(0);
        assertThat(assigneeForProsecutionIsDefendingCase.getEmail(), is(assigneeEmail));
    }

    @Test
    public void shouldRaiseAssigneeIsDefendingTheCaseEventWhenAssigneeIsDefendingForNonCps() {

        final String assigneeEmail = "not-exist-email@hmcts.net";
        final UUID caseId = randomUUID();

        final PersonDetails assigneePersonDetails = PersonDetails.personDetails()
                .withFirstName(ASSIGNEE_NAME)
                .withLastName(ASSIGNEE_LAST_NAME)
                .withUserId(randomUUID())
                .build();

        final Stream<Object> eventStream = advocate.assignCase(assigneeEmail, assigneePersonDetails, Organisation.organisation().withOrgId(randomUUID()).build(), null, null, caseId, of(DEFENCE_LAWYERS), true, randomUUID(), true, true, "DVLA");

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(AssigneeForProsecutionIsDefendingCase.class.getName()));

        final AssigneeForProsecutionIsDefendingCase assigneeForProsecutionIsDefendingCase = (AssigneeForProsecutionIsDefendingCase) eventList.get(0);
        assertThat(assigneeForProsecutionIsDefendingCase.getEmail(), is(assigneeEmail));
    }

    @Test
    public void shouldRaiseCaseAssignedToOrganisationWhenAssigneeIsDefenceLawyer() {

        final String assigneeEmail = "email@hmcts.net";
        final PersonDetails assigneePersonDetails = PersonDetails.personDetails()
                .withFirstName(ASSIGNEE_NAME)
                .withLastName(ASSIGNEE_LAST_NAME)
                .withUserId(randomUUID())
                .build();
        final PersonDetails assignerPersonDetails = PersonDetails.personDetails()
                .withFirstName(ASSIGNER_NAME)
                .withLastName(ASSIGNER_LAST_NAME)
                .withUserId(randomUUID())
                .build();

        final List<String> assigneeGroupList = of(DEFENCE_LAWYERS);
        final Organisation assigneeOrganisation = Organisation.organisation().withOrgId(UUID.randomUUID()).build();
        final Organisation assignerOrganisation = Organisation.organisation().withOrgId(randomUUID()).build();
        final UUID caseId = randomUUID();

        final Stream<Object> eventStream = advocate.assignCase(assigneeEmail, assigneePersonDetails, assigneeOrganisation, assignerOrganisation, assignerPersonDetails, caseId, assigneeGroupList, false, randomUUID(), true, true, "CPS");
        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(CaseAssignedToOrganisation.class.getName()));

        final CaseAssignedToOrganisation caseAssignedToOrganisation = (CaseAssignedToOrganisation) eventList.get(0);

        assertThat(caseAssignedToOrganisation.getAssigneeOrganisation(), is(assigneeOrganisation));
        assertThat(caseAssignedToOrganisation.getAssignorOrganisation(), is(assignerOrganisation));
        assertThat(caseAssignedToOrganisation.getAssigneeDetails(), is(assigneePersonDetails));
        assertThat(caseAssignedToOrganisation.getAssignorDetails(), is(assignerPersonDetails));
        assertThat(caseAssignedToOrganisation.getCaseId(), is(caseId));
    }

    @Test
    public void shouldRaiseCaseAssignedToOrganisationWhenAssigneeIsDefenceLawyerByNonCps() {

        final String assigneeEmail = "email@hmcts.net";
        final PersonDetails assigneePersonDetails = PersonDetails.personDetails()
                .withFirstName(ASSIGNEE_NAME)
                .withLastName(ASSIGNEE_LAST_NAME)
                .withUserId(randomUUID())
                .build();
        final PersonDetails assignerPersonDetails = PersonDetails.personDetails()
                .withFirstName(ASSIGNER_NAME)
                .withLastName(ASSIGNER_LAST_NAME)
                .withUserId(randomUUID())
                .build();

        final List<String> assigneeGroupList = of(DEFENCE_LAWYERS);
        final Organisation assigneeOrganisation = Organisation.organisation().withOrgId(UUID.randomUUID()).build();
        final Organisation assignerOrganisation = Organisation.organisation().withOrgId(randomUUID()).build();
        final UUID caseId = randomUUID();

        final Stream<Object> eventStream = advocate.assignCase(assigneeEmail, assigneePersonDetails, assigneeOrganisation, assignerOrganisation, assignerPersonDetails, caseId, assigneeGroupList, false, randomUUID(), true, true, "TFL");
        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(CaseAssignedToOrganisation.class.getName()));

        final CaseAssignedToOrganisation caseAssignedToOrganisation = (CaseAssignedToOrganisation) eventList.get(0);

        assertThat(caseAssignedToOrganisation.getAssigneeOrganisation(), is(assigneeOrganisation));
        assertThat(caseAssignedToOrganisation.getAssignorOrganisation(), is(assignerOrganisation));
        assertThat(caseAssignedToOrganisation.getAssigneeDetails(), is(assigneePersonDetails));
        assertThat(caseAssignedToOrganisation.getAssignorDetails(), is(assignerPersonDetails));
        assertThat(caseAssignedToOrganisation.getCaseId(), is(caseId));
    }

    @Test
    public void shouldRaiseCaseAssignedToAdvocateWhenAssigneeIsAdvocate() {

        final String assigneeEmail = "email@hmcts.net";
        final PersonDetails assigneePersonDetails = PersonDetails.personDetails()
                .withFirstName(ASSIGNEE_NAME)
                .withLastName(ASSIGNEE_LAST_NAME)
                .withUserId(randomUUID())
                .build();
        final PersonDetails assignerPersonDetails = PersonDetails.personDetails()
                .withFirstName(ASSIGNER_NAME)
                .withLastName(ASSIGNER_LAST_NAME)
                .withUserId(randomUUID())
                .build();

        final List<String> assigneeGroupList = of(ADVOCATES_ROLE);
        final Organisation assigneeOrganisation = Organisation.organisation().withOrgId(UUID.randomUUID()).build();
        final Organisation assignerOrganisation = Organisation.organisation().withOrgId(randomUUID()).build();
        final UUID caseId = randomUUID();

        final Stream<Object> eventStream = advocate.assignCase(assigneeEmail, assigneePersonDetails, assigneeOrganisation, assignerOrganisation, assignerPersonDetails, caseId, assigneeGroupList, false, randomUUID(), true, true, "CPS");
        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(CaseAssignedToAdvocate.class.getName()));

        final CaseAssignedToAdvocate caseAssignedToAdvocate = (CaseAssignedToAdvocate) eventList.get(0);

        assertThat(caseAssignedToAdvocate.getAssigneeOrganisation(), is(assigneeOrganisation));
        assertThat(caseAssignedToAdvocate.getAssignorOrganisation(), is(assignerOrganisation));
        assertThat(caseAssignedToAdvocate.getAssigneeDetails(), is(assigneePersonDetails));
        assertThat(caseAssignedToAdvocate.getAssignorDetails(), is(assignerPersonDetails));
        assertThat(caseAssignedToAdvocate.getCaseId(), is(caseId));
    }

    @Test
    public void shouldRaiseCaseAssignmentToAdvocateRemovedWhenAssigneeIsAdvocate() {

        final List<String> assigneeGroupList = of(ADVOCATES_ROLE);
        final UUID caseId = randomUUID();
        final UUID assigneeUserId = randomUUID();
        final UUID assigneeOrgId = randomUUID();
        final UUID removedByUserId = randomUUID();

        givenCaseAssigned(caseId, assigneeUserId, assigneeOrgId);
        final Stream<Object> eventStream = advocate.removeCaseAssignment(caseId, assigneeUserId, assigneeGroupList, false, removedByUserId, false);
        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(CaseAssignmentToAdvocateRemoved.class.getName()));

        final CaseAssignmentToAdvocateRemoved caseAssignmentToAdvocateRemoved = (CaseAssignmentToAdvocateRemoved) eventList.get(0);

        assertThat(caseAssignmentToAdvocateRemoved.getCaseId(), is(caseId));
        assertThat(caseAssignmentToAdvocateRemoved.getAssigneeUserId(), is(assigneeUserId));
        assertThat(caseAssignmentToAdvocateRemoved.getAssigneeOrganisationId(), is(assigneeOrgId));
        assertThat(caseAssignmentToAdvocateRemoved.getRemovedByUserId(), is(removedByUserId));
        assertThat(caseAssignmentToAdvocateRemoved.getRemovedTimestamp(), is(notNullValue()));
        assertThat(caseAssignmentToAdvocateRemoved.getIsAutomaticUnassignment(), is(false));

    }

    @Test
    public void shouldRaiseCaseAssignmentToOrganisationRemovedWhenAssigneeIsDefenceLawyer() {

        final UUID caseId = randomUUID();
        final UUID assigneeUserId = randomUUID();
        final UUID assigneeOrgId = randomUUID();
        final UUID removedByUserId = randomUUID();
        final List<String> assigneeGroupList = of(DEFENCE_LAWYERS);

        givenCaseAssigned(caseId, assigneeUserId, assigneeOrgId);
        final Stream<Object> eventStream = advocate.removeCaseAssignment(caseId, assigneeUserId, assigneeGroupList, false, removedByUserId, false);

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(CaseAssigmentToOrganisationRemoved.class.getName()));

        final CaseAssigmentToOrganisationRemoved caseAssigmentToOrganisationRemoved = (CaseAssigmentToOrganisationRemoved) eventList.get(0);

        assertThat(caseAssigmentToOrganisationRemoved.getCaseId(), is(caseId));
        assertThat(caseAssigmentToOrganisationRemoved.getAssigneeOrganisationId(), is(assigneeOrgId));
        assertThat(caseAssigmentToOrganisationRemoved.getRemovedByUserId(), is(removedByUserId));
        assertThat(caseAssigmentToOrganisationRemoved.getRemovedTimestamp(), is(notNullValue()));
        assertThat(caseAssigmentToOrganisationRemoved.getIsAutomaticUnassignment(), is(false));
    }

    @Test
    public void shouldNotRaiseRemoveCaseAssignmentToOrganisationFailedWhenAssigneeIsDefenceLawyerAndHasOtherAdvocatesAssignedToTheCase() {

        final UUID caseId = randomUUID();
        final UUID assigneeUserId = randomUUID();
        final UUID assigneeOrgId = randomUUID();
        final UUID removedByUserId = randomUUID();
        final List<String> assigneeGroupList = of(DEFENCE_LAWYERS);
        final boolean hasAdvocatesAssignedToTheCase = true;

        givenCaseAssigned(caseId, assigneeUserId, assigneeOrgId);
        final Stream<Object> eventStream = advocate.removeCaseAssignment(caseId, assigneeUserId, assigneeGroupList, hasAdvocatesAssignedToTheCase, removedByUserId, true);

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(0));
    }

    @Test
    public void givenUserNotPreviouslyAssignedToTheCase_shouldRaiseUserNotAssignedEventWhenAssigneeIsDefenceLawyer() {

        final UUID caseId = randomUUID();
        final UUID assigneeUserId = randomUUID();
        final UUID removedByUserId = randomUUID();
        final List<String> assigneeGroupList = of(DEFENCE_LAWYERS);

        final Stream<Object> eventStream = advocate.removeCaseAssignment(caseId, assigneeUserId, assigneeGroupList, false, removedByUserId, false);

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(UserNotAssigned.class.getName()));

        final UserNotAssigned userNotAssigned = (UserNotAssigned) eventList.get(0);

        assertThat(userNotAssigned.getAssigneeUserId(), is(assigneeUserId));
        assertThat(userNotAssigned.getErrorCode(), is(Integer.valueOf(ErrorType.USER_NOT_ASSIGNED.getCode())));
        assertThat(userNotAssigned.getFailureReason(), is(ErrorType.USER_NOT_ASSIGNED.getMessage()));
    }

    @Test
    void shouldNotRaiseUserNotAssignedEventWhenAssigneeIsDefenceLawyerWhenAutomaticUnassignment() {

        final UUID caseId = randomUUID();
        final UUID assigneeUserId = randomUUID();
        final UUID removedByUserId = randomUUID();
        final List<String> assigneeGroupList = of(DEFENCE_LAWYERS);

        final Stream<Object> eventStream = advocate.removeCaseAssignment(caseId, assigneeUserId, assigneeGroupList, false, removedByUserId, true);

        final List<?> eventList = eventStream.toList();
        assertThat(eventList.isEmpty(), is(true));
    }

    @Test
    public void assignCasesShouldRaiseFailedEventWhenAssigneeDetailsAlreadyHaveErrorsCapturedFromApiLevel() {

        final String assigneeEmail = "not-exist-email@hmcts.net";

        List<CaseHearingAssignmentDetails> caseHearingAssignmentDetails = of(new CaseHearingAssignmentDetails(randomUUID(), caseId,
                hearingId, false, false, false, prosecutionAuthId, ErrorType.CASE_NOT_FOUND.name(), parseInt(ErrorType.CASE_NOT_FOUND.getCode())));

        final Stream<Object> eventStream = advocate.assignCaseHearing(assigneeEmail, null, null, null, null, null, caseHearingAssignmentDetails, "CPS");

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(CaseAssignmentsByHearingListingFailed.class.getName()));

        final CaseAssignmentsByHearingListingFailed caseAssignmentsByHearingListingFailed = (CaseAssignmentsByHearingListingFailed) eventList.get(0);
        assertThat(caseAssignmentsByHearingListingFailed.getEmail(), is(assigneeEmail));
        assertCaseAssignmentsFailedEvent(caseAssignmentsByHearingListingFailed, caseId, hearingId, ErrorType.CASE_NOT_FOUND);
    }

    @Test
    public void assignCasesShouldRaiseUserNotFoundWhenAssigneeDetailsIsNotAvailable() {

        final String assigneeEmail = "not-exist-email@hmcts.net";

        List<CaseHearingAssignmentDetails> caseHearingAssignmentDetails = of(new CaseHearingAssignmentDetails(randomUUID(), caseId,
                hearingId, false, false, false, prosecutionAuthId, null, null));

        final Stream<Object> eventStream = advocate.assignCaseHearing(assigneeEmail, null, null, null, null, null, caseHearingAssignmentDetails, "CPS");

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(CaseAssignmentsByHearingListingFailed.class.getName()));

        final CaseAssignmentsByHearingListingFailed caseAssignmentsByHearingListingFailed = (CaseAssignmentsByHearingListingFailed) eventList.get(0);
        assertThat(caseAssignmentsByHearingListingFailed.getEmail(), is(assigneeEmail));
        assertCaseAssignmentsFailedEvent(caseAssignmentsByHearingListingFailed, caseId, hearingId, ErrorType.USER_NOT_FOUND);
    }

    @Test
    public void assignCasesShouldRaiseAssigneeUserNotInAllowedGroupsWhenAssigneeIsNotDefenceLawyerOrAdvocate() {

        final String assigneeEmail = "email@hmcts.net";
        final UUID assigneeUserId = UUID.randomUUID();
        final PersonDetails assigneePersonDetails = getPersonDetails(assigneeUserId);

        final List<String> assigneeGroupList = of("System Users");

        List<CaseHearingAssignmentDetails> caseHearingAssignmentDetails = of(new CaseHearingAssignmentDetails(randomUUID(), caseId,
                hearingId, false, false, false, prosecutionAuthId, null, null));

        final Stream<Object> eventStream = advocate.assignCaseHearing(assigneeEmail, assigneePersonDetails, null, assigneeGroupList, null, null, caseHearingAssignmentDetails, "CPS");

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(CaseAssignmentsByHearingListingFailed.class.getName()));

        final CaseAssignmentsByHearingListingFailed caseAssignmentsByHearingListingFailed = (CaseAssignmentsByHearingListingFailed) eventList.get(0);
        assertThat(caseAssignmentsByHearingListingFailed.getEmail(), is(assigneeEmail));
        assertCaseAssignmentsFailedEvent(caseAssignmentsByHearingListingFailed, caseId, hearingId, ErrorType.ASSIGNEE_NOT_IN_ALLOWED_GROUPS);
    }

    @Test
    public void assignCasesShouldRaiseAssigneeIsDefendingTheCaseEventWhenAssigneeIsDefending() {

        final String assigneeEmail = "not-exist-email@hmcts.net";
        final PersonDetails assigneePersonDetails = getPersonDetails(randomUUID());

        final List<CaseHearingAssignmentDetails> caseHearingAssignmentDetails = of(new CaseHearingAssignmentDetails(randomUUID(), caseId,
                hearingId, true, true, true, prosecutionAuthId, null, null));
        final Stream<Object> eventStream = advocate.assignCaseHearing(assigneeEmail, assigneePersonDetails, Organisation.organisation().withOrgId(randomUUID()).build(), of(DEFENCE_LAWYERS), null, null, caseHearingAssignmentDetails, "CPS");

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(CaseAssignmentsByHearingListingFailed.class.getName()));

        final CaseAssignmentsByHearingListingFailed caseAssignmentsByHearingListingFailed = (CaseAssignmentsByHearingListingFailed) eventList.get(0);
        assertThat(caseAssignmentsByHearingListingFailed.getEmail(), is(assigneeEmail));
        assertCaseAssignmentsFailedEvent(caseAssignmentsByHearingListingFailed, caseId, hearingId, ErrorType.ASSIGNEE_DEFENDING_CASE);
    }

    @Test
    public void assignCasesShouldRaiseCaseAssignedToOrganisationWhenAssigneeIsDefenceLawyer() {

        final String assigneeEmail = "email@hmcts.net";
        final PersonDetails assigneePersonDetails = getPersonDetails(randomUUID());
        final PersonDetails assignerPersonDetails = getPersonDetails(randomUUID());

        final List<String> assigneeGroupList = of(DEFENCE_LAWYERS);
        final Organisation assigneeOrganisation = Organisation.organisation().withOrgId(UUID.randomUUID()).build();
        final Organisation assignerOrganisation = Organisation.organisation().withOrgId(randomUUID()).build();

        final List<CaseHearingAssignmentDetails> caseHearingAssignmentDetails = of(new CaseHearingAssignmentDetails(randomUUID(), caseId,
                hearingId, false, true, true, prosecutionAuthId, null, null));
        final Stream<Object> eventStream = advocate.assignCaseHearing(assigneeEmail, assigneePersonDetails, assigneeOrganisation, assigneeGroupList, assignerPersonDetails, assignerOrganisation, caseHearingAssignmentDetails, "CPS");

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(CasesAssignedToOrganisation.class.getName()));

        final CasesAssignedToOrganisation casesAssignedToOrganisation = (CasesAssignedToOrganisation) eventList.get(0);

        assertThat(casesAssignedToOrganisation.getAssigneeOrganisation(), is(assigneeOrganisation));
        assertThat(casesAssignedToOrganisation.getAssignorOrganisation(), is(assignerOrganisation));
        assertThat(casesAssignedToOrganisation.getAssigneeDetails(), is(assigneePersonDetails));
        assertThat(casesAssignedToOrganisation.getAssignorDetails(), is(assignerPersonDetails));
        assertThat(casesAssignedToOrganisation.getCaseHearingAssignments().get(0).getCaseId(), is(caseId));
        assertThat(casesAssignedToOrganisation.getCaseHearingAssignments().get(0).getHearingId(), is(hearingId));
        assertThat(casesAssignedToOrganisation.getCaseHearingAssignments().get(0).getIsCps(), is(true));
        assertThat(casesAssignedToOrganisation.getCaseHearingAssignments().get(0).getIsPolice(), is(true));
        assertThat(casesAssignedToOrganisation.getCaseHearingAssignments().get(0).getProsecutingAuthorityId(), is(prosecutionAuthId));
    }

    @Test
    public void assignCasesShouldRaiseCaseAssignedToAdvocateWhenAssigneeIsAdvocate() {

        final String assigneeEmail = "email@hmcts.net";
        final PersonDetails assigneePersonDetails = getPersonDetails(randomUUID());
        final PersonDetails assignerPersonDetails = getPersonDetails(randomUUID());

        final List<String> assigneeGroupList = of(ADVOCATES_ROLE);
        final Organisation assigneeOrganisation = Organisation.organisation().withOrgId(UUID.randomUUID()).build();
        final Organisation assignerOrganisation = Organisation.organisation().withOrgId(randomUUID()).build();

        final List<CaseHearingAssignmentDetails> caseHearingAssignmentDetails = of(new CaseHearingAssignmentDetails(randomUUID(), caseId,
                hearingId, false, true, true, prosecutionAuthId, null, null));
        final Stream<Object> eventStream = advocate.assignCaseHearing(assigneeEmail, assigneePersonDetails, assigneeOrganisation, assigneeGroupList, assignerPersonDetails, assignerOrganisation, caseHearingAssignmentDetails, "CPS");

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(CasesAssignedToAdvocate.class.getName()));

        final CasesAssignedToAdvocate casesAssignedToAdvocate = (CasesAssignedToAdvocate) eventList.get(0);

        assertThat(casesAssignedToAdvocate.getAssigneeOrganisation(), is(assigneeOrganisation));
        assertThat(casesAssignedToAdvocate.getAssignorOrganisation(), is(assignerOrganisation));
        assertThat(casesAssignedToAdvocate.getAssigneeDetails(), is(assigneePersonDetails));
        assertThat(casesAssignedToAdvocate.getAssignorDetails(), is(assignerPersonDetails));

        assertThat(casesAssignedToAdvocate.getCaseHearingAssignments().get(0).getCaseId(), is(caseId));
        assertThat(casesAssignedToAdvocate.getCaseHearingAssignments().get(0).getHearingId(), is(hearingId));
        assertThat(casesAssignedToAdvocate.getCaseHearingAssignments().get(0).getIsCps(), is(true));
        assertThat(casesAssignedToAdvocate.getCaseHearingAssignments().get(0).getIsPolice(), is(true));
        assertThat(casesAssignedToAdvocate.getCaseHearingAssignments().get(0).getProsecutingAuthorityId(), is(prosecutionAuthId));
    }

    private void givenCaseAssigned(UUID caseId, UUID assigneeUserId, UUID assigneeOrganisationId) {
        final String assigneeEmail = "email@hmcts.net";
        final PersonDetails assigneePersonDetails = PersonDetails.personDetails()
                .withUserId(assigneeUserId)
                .build();
        final PersonDetails assignerPersonDetails = PersonDetails.personDetails()
                .withUserId(randomUUID())
                .build();

        final List<String> assigneeGroupList = of(ADVOCATES_ROLE);
        final Organisation assigneeOrganisation = Organisation.organisation().withOrgId(assigneeOrganisationId).build();
        final Organisation assignerOrganisation = Organisation.organisation().withOrgId(randomUUID()).build();

        advocate.assignCase(assigneeEmail, assigneePersonDetails, assigneeOrganisation, assignerOrganisation, assignerPersonDetails, caseId, assigneeGroupList, false, randomUUID(), true, true, "CPS");
    }

    private void assertCaseAssignmentsFailedEvent(final CaseAssignmentsByHearingListingFailed caseAssignmentsByHearingListingFailed, final UUID caseId,
                                                  final UUID hearingId, final ErrorType errorType) {
        assertThat(caseAssignmentsByHearingListingFailed.getAssignmentErrors().size(), is(1));
        assertThat(caseAssignmentsByHearingListingFailed.getAssignmentErrors().get(0).getCaseId(), is(caseId));
        assertThat(caseAssignmentsByHearingListingFailed.getAssignmentErrors().get(0).getHearingId(), is(hearingId));
        assertThat(caseAssignmentsByHearingListingFailed.getAssignmentErrors().get(0).getErrorCode(), is(parseInt(errorType.getCode())));
        assertThat(caseAssignmentsByHearingListingFailed.getAssignmentErrors().get(0).getFailureReason(), is(errorType.name()));
    }

    private PersonDetails getPersonDetails(UUID assigneeUserId) {
        return PersonDetails.personDetails()
                .withFirstName("assignee_name")
                .withLastName("assignee_lastname")
                .withUserId(assigneeUserId)
                .build();
    }
}
