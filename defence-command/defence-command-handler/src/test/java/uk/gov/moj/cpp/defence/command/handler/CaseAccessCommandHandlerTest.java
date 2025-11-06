package uk.gov.moj.cpp.defence.command.handler;


import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.moj.cpp.defence.command.handler.CaseAccessCommandHandler.ASSIGNEE_USER_ID;
import static uk.gov.moj.cpp.defence.command.handler.CaseAccessCommandHandler.CASE_ID;

import uk.gov.justice.cps.defence.PersonDetails;
import uk.gov.justice.cps.defence.commands.AssignCase;
import uk.gov.justice.cps.defence.commands.AssignCaseByHearing;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.defence.CaseAssignmentDetails;
import uk.gov.moj.cpp.defence.CaseHearingAssignmentDetails;
import uk.gov.moj.cpp.defence.Organisation;
import uk.gov.moj.cpp.defence.aggregate.Advocate;
import uk.gov.moj.cpp.defence.service.DefenceService;
import uk.gov.moj.cpp.defence.service.UserGroupService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.Json;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)

public class CaseAccessCommandHandlerTest {
    private static final String DEFENCE_COMMAND_HANDLER_ADVOCATE_ASSIGN_CASE = "defence.command.handler.advocate.assign-case";
    private static final String DEFENCE_COMMAND_HANDLER_ADVOCATE_REMOVE_CASE_ASSIGNMENT = "defence.command.handler.advocate.remove-case-assignment";
    private static final String DEFENCE_COMMAND_HANDLER_ADVOCATE_ASSIGN_CASE_BY_HEARING_LISTING = "defence.command.handler.advocate.assign-case-by-hearing-listing";
    private static final String ORGANISATION_NAME = "CompanyZ";
    private static final String EMAIL_ID = "email@hmcts.net";

    @Mock
    Advocate advocate;
    @Mock
    private UserGroupService usersGroupService;
    @Mock
    private EventSource eventSource;
    @Mock
    private EventStream eventStream;
    @Mock
    private AggregateService aggregateService;
    @InjectMocks
    private CaseAccessCommandHandler caseAccessCommandHandler;
    @Mock
    private Stream<Object> newEvents;

    @Mock
    private Stream<Object> mappedNewEvents;

    @Captor
    private ArgumentCaptor<Stream<JsonEnvelope>> argumentCaptorStream;

    @Mock
    private DefenceService defenceService;

    @Test
    public void shouldHandleCommandAssignCase() {
        assertThat(new CaseAccessCommandHandler(), isHandler(COMMAND_HANDLER)
                .with(method("assignCase")
                        .thatHandles(DEFENCE_COMMAND_HANDLER_ADVOCATE_ASSIGN_CASE)
                ));
    }

    @Test
    public void shouldAssignCaseSuccessfully() throws Exception {
        final UUID userId = UUID.randomUUID();
        final UUID assigneeUserId = UUID.randomUUID();
        final UUID caseId = randomUUID();
        final UUID prosecutingAuthorityId = randomUUID();
        final Envelope<AssignCase> envelope = createAssignCaseEnvelope(userId, caseId, prosecutingAuthorityId);

        PersonDetails assigneeDetails = createPersonDetails(assigneeUserId);
        Organisation assigneeOrg = createOrganisation(assigneeUserId);
        Organisation assignorOrg = createOrganisation(userId);
        PersonDetails assignorDetails = createAssignorDetails(userId);

        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Advocate.class)).thenReturn(advocate);
        when(newEvents.map(any())).thenReturn(mappedNewEvents);
        when(usersGroupService.getUserDetailsWithEmail(eq(EMAIL_ID), any(), any())).thenReturn(assigneeDetails);
        when(usersGroupService.getGroupNamesForUser(eq(assigneeUserId), any(), any())).thenReturn(getGroupNames());
        when(usersGroupService.getUserDetailsWithUserId(eq(userId), any(), any())).thenReturn(assignorDetails);
        when(usersGroupService.getOrganisationDetailsForUser(eq(userId), any(), any())).thenReturn(assignorOrg);
        when(usersGroupService.getOrganisationDetailsForUser(eq(assigneeUserId), any(), any())).thenReturn(assigneeOrg);
        when(advocate.assignCase(EMAIL_ID, assigneeDetails, assigneeOrg, assignorOrg, assignorDetails, caseId, getGroupNames(), false, prosecutingAuthorityId, true, true, "CPS")).thenReturn(newEvents);

        caseAccessCommandHandler.assignCase(envelope);

        checkEventsAppendedAreMappedNewEvents();
    }

    @Test
    public void shouldAssignCaseSuccessfullyForNonCps() throws Exception {
        final UUID userId = UUID.randomUUID();
        final UUID assigneeUserId = UUID.randomUUID();
        final UUID caseId = randomUUID();
        final UUID prosecutingAuthorityId = randomUUID();
        final Envelope<AssignCase> envelope = createAssignCaseEnvelopeForNonCps(userId, caseId, prosecutingAuthorityId);

        PersonDetails assigneeDetails = createPersonDetails(assigneeUserId);
        Organisation assigneeOrg = createOrganisation(assigneeUserId);
        Organisation assignorOrg = createOrganisation(userId);
        PersonDetails assignorDetails = createAssignorDetails(userId);

        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Advocate.class)).thenReturn(advocate);
        when(newEvents.map(any())).thenReturn(mappedNewEvents);
        when(usersGroupService.getUserDetailsWithEmail(eq(EMAIL_ID), any(), any())).thenReturn(assigneeDetails);
        when(usersGroupService.getGroupNamesForUser(eq(assigneeUserId), any(), any())).thenReturn(getGroupNames());
        when(usersGroupService.getUserDetailsWithUserId(eq(userId), any(), any())).thenReturn(assignorDetails);
        when(usersGroupService.getOrganisationDetailsForUser(eq(userId), any(), any())).thenReturn(assignorOrg);
        when(usersGroupService.getOrganisationDetailsForUser(eq(assigneeUserId), any(), any())).thenReturn(assigneeOrg);
        when(advocate.assignCase(EMAIL_ID, assigneeDetails, assigneeOrg, assignorOrg, assignorDetails, caseId, getGroupNames(), false, prosecutingAuthorityId, true, true, "DVLA")).thenReturn(newEvents);

        caseAccessCommandHandler.assignCase(envelope);

        checkEventsAppendedAreMappedNewEvents();
    }

    @Test
    public void shouldHandleCommandRemoveCaseAssignment() {
        assertThat(new CaseAccessCommandHandler(), isHandler(COMMAND_HANDLER)
                .with(method("removeCaseAssignment")
                        .thatHandles(DEFENCE_COMMAND_HANDLER_ADVOCATE_REMOVE_CASE_ASSIGNMENT)
                ));
    }

    @Test
    public void handleRemoveCaseAssignmentShouldThrowExceptionWhenFailedToFindUserIdInTheEvent() throws EventStreamException {

        final UUID caseId = randomUUID();
        final UUID assigneeId = randomUUID();
        final UUID removedByUserId = null;
        final JsonEnvelope jsonEnvelope = getRemoveCaseAssignmentJsonEnvelope(caseId, assigneeId, removedByUserId);

        assertThrows(IllegalArgumentException.class, () -> caseAccessCommandHandler.removeCaseAssignment(jsonEnvelope));
    }

    @Test
    public void handleRemoveCaseAssignment() throws EventStreamException {

        final UUID caseId = randomUUID();
        final UUID assigneeId = randomUUID();
        final UUID removedByUserId = randomUUID();

        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Advocate.class)).thenReturn(advocate);
        when(newEvents.map(any())).thenReturn(mappedNewEvents);
        when(advocate.getAssigneeOrganisationId()).thenReturn(randomUUID());
        when(usersGroupService.getGroupNamesForUser(eq(assigneeId), any(), any())).thenReturn(getGroupNames());

        when(advocate.removeCaseAssignment(caseId, assigneeId, getGroupNames(), false, removedByUserId, false)).thenReturn(newEvents);

        final JsonEnvelope jsonEnvelope = getRemoveCaseAssignmentJsonEnvelope(caseId, assigneeId, removedByUserId);

        caseAccessCommandHandler.removeCaseAssignment(jsonEnvelope);

        checkEventsAppendedAreMappedNewEvents();
    }

    @Test
    public void shouldAssignCaseByHearingListingSuccessfully() throws Exception {
        final UUID userId = UUID.randomUUID();
        final UUID assigneeUserId = UUID.randomUUID();
        final UUID caseId = randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final UUID prosecutingAuthorityId = randomUUID();
        final Envelope<AssignCaseByHearing> envelope = createAssignCaseByHearingEnvelope(userId, caseId, hearingId, prosecutingAuthorityId);

        final PersonDetails assigneeDetails = createPersonDetails(assigneeUserId);
        final Organisation assigneeOrg = createOrganisation(assigneeUserId);
        final Organisation assignorOrg = createOrganisation(userId);
        final PersonDetails assignorDetails = createAssignorDetails(userId);

        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Advocate.class)).thenReturn(advocate);
        when(newEvents.map(any())).thenReturn(mappedNewEvents);
        when(usersGroupService.getUserDetailsWithEmail(eq(EMAIL_ID), any(), any())).thenReturn(assigneeDetails);
        when(usersGroupService.getGroupNamesForUser(eq(assigneeUserId), any(), any())).thenReturn(getGroupNames());
        when(usersGroupService.getUserDetailsWithUserId(eq(userId), any(), any())).thenReturn(assignorDetails);
        when(usersGroupService.getOrganisationDetailsForUser(eq(userId), any(), any())).thenReturn(assignorOrg);
        when(usersGroupService.getOrganisationDetailsForUser(eq(assigneeUserId), any(), any())).thenReturn(assigneeOrg);
        when(advocate.assignCaseHearing(eq(EMAIL_ID), eq(assigneeDetails), eq(assigneeOrg), eq(getGroupNames()), eq(assignorDetails), eq(assignorOrg), anyList(), any())).thenReturn(newEvents);

        caseAccessCommandHandler.assignCaseByHearingListing(envelope);

        checkEventsAppendedAreMappedNewEvents();
    }


    @Test
    public void shouldAssignCaseByHearingListingSuccessfullyWithRepresentingOrganisation() throws Exception {
        final UUID userId = UUID.randomUUID();
        final UUID assigneeUserId = UUID.randomUUID();
        final UUID caseId = randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final UUID prosecutingAuthorityId = randomUUID();
        final Envelope<AssignCaseByHearing> envelope = createAssignCaseByHearingWithRepresentingOrgEnvelope(userId, caseId, hearingId, prosecutingAuthorityId);

        final PersonDetails assigneeDetails = createPersonDetails(assigneeUserId);
        final Organisation assigneeOrg = createOrganisation(assigneeUserId);
        final Organisation assignorOrg = createOrganisation(userId);
        final PersonDetails assignorDetails = createAssignorDetails(userId);

        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, Advocate.class)).thenReturn(advocate);
        when(newEvents.map(any())).thenReturn(mappedNewEvents);


        when(usersGroupService.getUserDetailsWithEmail(eq(EMAIL_ID), any(), any())).thenReturn(assigneeDetails);
        when(usersGroupService.getGroupNamesForUser(eq(assigneeUserId), any(), any())).thenReturn(getGroupNames());
        when(usersGroupService.getUserDetailsWithUserId(eq(userId), any(), any())).thenReturn(assignorDetails);
        when(usersGroupService.getOrganisationDetailsForUser(eq(userId), any(), any())).thenReturn(assignorOrg);
        when(usersGroupService.getOrganisationDetailsForUser(eq(assigneeUserId), any(), any())).thenReturn(assigneeOrg);
        when(advocate.assignCaseHearing(eq(EMAIL_ID), eq(assigneeDetails), eq(assigneeOrg), eq(getGroupNames()), eq(assignorDetails), eq(assignorOrg), anyList(), any())).thenReturn(newEvents);

        caseAccessCommandHandler.assignCaseByHearingListing(envelope);

        checkEventsAppendedAreMappedNewEvents();
    }


    private void checkEventsAppendedAreMappedNewEvents() throws EventStreamException {
        verify(eventStream).append(argumentCaptorStream.capture());
        final Stream<JsonEnvelope> stream = argumentCaptorStream.getValue();
        MatcherAssert.assertThat(stream, Is.is(mappedNewEvents));
    }


    private List<String> getGroupNames() {
        return Arrays.asList("Chambers Admin", "System Users");
    }

    private PersonDetails createPersonDetails(final UUID userId) {
        return PersonDetails.personDetails()
                .withFirstName("assignee_name")
                .withLastName("assignee_lastname")
                .withUserId(userId)
                .build();
    }

    private PersonDetails createAssignorDetails(final UUID userId) {
        return PersonDetails.personDetails()
                .withFirstName("assigner_name")
                .withLastName("assigner_lastname")
                .withUserId(userId)
                .build();
    }

    private Organisation createOrganisation(final UUID orgId) {
        return Organisation.organisation().withOrgId(orgId).withOrganisationName(ORGANISATION_NAME).build();
    }

    private Envelope<AssignCase> createAssignCaseEnvelope(final UUID userId, final UUID caseId, final UUID prosecutingAuthorityId) {
        AssignCase assignCase = AssignCase.assignCase()
                .withAssignorId(userId)
                .withAssigneeEmailId(EMAIL_ID + " ")
                .withCaseAssignmentDetails(of(CaseAssignmentDetails.caseAssignmentDetails()
                        .withCaseId(caseId)
                        .withIsAssigneeDefendingCase(false)
                        .withProsecutionAuthorityId(prosecutingAuthorityId)
                        .withIsCps(true)
                        .withIsPolice(true)
                        .build()))
                .build();
        final Metadata metadata = Envelope
                .metadataBuilder()
                .withName(DEFENCE_COMMAND_HANDLER_ADVOCATE_ASSIGN_CASE)
                .withId(randomUUID())
                .withUserId(userId.toString())
                .build();
        return envelopeFrom(metadata, assignCase);
    }

    private Envelope<AssignCase> createAssignCaseEnvelopeForNonCps(final UUID userId, final UUID caseId, final UUID prosecutingAuthorityId) {
        AssignCase assignCase = AssignCase.assignCase()
                .withAssignorId(userId)
                .withAssigneeEmailId(EMAIL_ID)
                .withCaseAssignmentDetails(of(CaseAssignmentDetails.caseAssignmentDetails()
                        .withCaseId(caseId)
                        .withIsAssigneeDefendingCase(false)
                        .withProsecutionAuthorityId(prosecutingAuthorityId)
                        .withIsCps(true)
                        .withIsPolice(true)
                        .withRepresentingOrganisation("DVLA")
                        .build()))
                .build();
        final Metadata metadata = Envelope
                .metadataBuilder()
                .withName(DEFENCE_COMMAND_HANDLER_ADVOCATE_ASSIGN_CASE)
                .withId(randomUUID())
                .withUserId(userId.toString())
                .build();
        return envelopeFrom(metadata, assignCase);
    }

    private Envelope<AssignCaseByHearing> createAssignCaseByHearingEnvelope(final UUID userId, final UUID caseId, final UUID hearingId, final UUID prosecutingAuthorityId) {
        AssignCaseByHearing assignCase = AssignCaseByHearing.assignCaseByHearing()
                .withAssignorId(userId)
                .withAssigneeEmailId(EMAIL_ID)
                .withCaseHearingAssignmentDetails(singletonList(CaseHearingAssignmentDetails.caseHearingAssignmentDetails()
                        .withCaseId(caseId)
                        .withHearingId(hearingId)
                        .withIsAssigneeDefendingCase(false)
                        .withProsecutionAuthorityId(prosecutingAuthorityId)
                        .withIsCps(true)
                        .withIsPolice(true)
                        .build()))
                .build();
        final Metadata metadata = Envelope
                .metadataBuilder()
                .withName(DEFENCE_COMMAND_HANDLER_ADVOCATE_ASSIGN_CASE_BY_HEARING_LISTING)
                .withId(randomUUID())
                .withUserId(userId.toString())
                .build();
        return envelopeFrom(metadata, assignCase);
    }

    private Envelope<AssignCaseByHearing> createAssignCaseByHearingWithRepresentingOrgEnvelope(final UUID userId, final UUID caseId, final UUID hearingId, final UUID prosecutingAuthorityId) {
        AssignCaseByHearing assignCase = AssignCaseByHearing.assignCaseByHearing()
                .withAssignorId(userId)
                .withAssigneeEmailId(EMAIL_ID)
                .withCaseHearingAssignmentDetails(singletonList(CaseHearingAssignmentDetails.caseHearingAssignmentDetails()
                        .withCaseId(caseId)
                        .withHearingId(hearingId)
                        .withIsAssigneeDefendingCase(false)
                        .withProsecutionAuthorityId(prosecutingAuthorityId)
                        .withIsCps(true)
                        .withIsPolice(true)
                        .withRepresentingOrganisation("TFL")
                        .build()))
                .build();
        final Metadata metadata = Envelope
                .metadataBuilder()
                .withName(DEFENCE_COMMAND_HANDLER_ADVOCATE_ASSIGN_CASE_BY_HEARING_LISTING)
                .withId(randomUUID())
                .withUserId(userId.toString())
                .build();
        return envelopeFrom(metadata, assignCase);
    }

    private JsonEnvelope getRemoveCaseAssignmentJsonEnvelope(UUID caseId, UUID assigneeId, UUID removedByUserId) {
        MetadataBuilder metadataBuilder = JsonEnvelope.metadataBuilder()
                .withId(randomUUID())
                .withName("defence.command.handler.advocate.remove-case-assignment");
        if (nonNull(removedByUserId)) {
            metadataBuilder.withUserId(removedByUserId.toString());
        }
        return JsonEnvelope.envelopeFrom(
                metadataBuilder.build(),
                Json.createObjectBuilder()
                        .add(CASE_ID, caseId.toString())
                        .add(ASSIGNEE_USER_ID, assigneeId.toString())
                        .build());
    }

}
