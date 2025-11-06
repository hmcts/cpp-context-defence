package uk.gov.moj.cpp.defence.command.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.cps.defence.AddOffencePleas;
import uk.gov.justice.cps.defence.OffencePleaDetails;
import uk.gov.justice.cps.defence.PleasAllocationDetails;
import uk.gov.justice.cps.defence.UpdateOffencePleas;
import uk.gov.justice.cps.defence.YesNoNa;
import uk.gov.justice.cps.defence.plea.PleaDefendantDetails;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher;
import uk.gov.moj.cpp.defence.aggregate.DefenceClient;
import uk.gov.moj.cpp.defence.aggregate.DefencePleaAggregate;
import uk.gov.moj.cpp.defence.events.AllocationPleasAdded;
import uk.gov.moj.cpp.defence.events.AllocationPleasUpdated;
import uk.gov.moj.cpp.defence.events.OpaTaskRequested;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;

@ExtendWith(MockitoExtension.class)
public class DefencePleaCommandHandlerTest {
    private static final String COMMAND_HANDLER_OFFENCE_PLEAS_ADD = "defence.command.add-offence-pleas";
    private static final String COMMAND_HANDLER_OFFENCE_PLEAS_UPDATE = "defence.command.update-offence-pleas";
    private static final UUID CASE_ID = randomUUID();
    @Spy
    private final Enveloper enveloper = EnveloperFactory.createEnveloperWithEvents(
            AllocationPleasAdded.class, AllocationPleasUpdated.class, OpaTaskRequested.class);
    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream eventStream;

    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private DefencePleaCommandHandler defencePleaCommandHandler;

    private DefencePleaAggregate defencePleaAggregate;

    private DefenceClient defenceClientAggregate;

    @BeforeEach
    public void setup() {

        defencePleaAggregate = new DefencePleaAggregate();
        defenceClientAggregate = new DefenceClient();
        lenient().when(eventSource.getStreamById(any())).thenReturn(eventStream);
        lenient().when(aggregateService.get(eventStream, DefencePleaAggregate.class)).thenReturn(defencePleaAggregate);
        lenient().when(aggregateService.get(eventStream, DefenceClient.class)).thenReturn(defenceClientAggregate);
    }

    @Test
    public void shouldHandleCommandCreatePlea() {
        assertThat(new DefencePleaCommandHandler(), isHandler(COMMAND_HANDLER)
                .with(method("createPlea")
                        .thatHandles(COMMAND_HANDLER_OFFENCE_PLEAS_ADD)
                ));
        //TODO add  assertion
    }

    @Test
    public void shouldCreatePleaSuccessfully() throws Exception {
        final UUID userId = UUID.randomUUID();
        final Envelope<AddOffencePleas> envelope = createAddOffencePleasEnvelope(userId);
        defenceClientAggregate.receiveADefenceClient(null, null, uk.gov.justice.cps.defence.DefendantDetails.defendantDetails().withCaseId(CASE_ID).build(), null, null);

        defencePleaCommandHandler.createPlea(envelope);
        final Stream<JsonEnvelope> jsonEnvelopeStream = verifyAppendAndGetArgumentFrom(eventStream);
        final List<JsonEnvelope> eventList = jsonEnvelopeStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(2));
        JsonEnvelope eventJsonEnvelope = eventList.get(0);
        assertThat(eventJsonEnvelope.metadata().name(), is("defence.event.allocation-pleas-added"));
        assertThat(eventJsonEnvelope.payloadAsJsonObject().getJsonObject("pleasAllocation").getString("caseId"), is(CASE_ID.toString()));
        eventJsonEnvelope = eventList.get(1);
        assertThat(eventJsonEnvelope.metadata().name(), is("defence.event.opa-task-requested"));
    }

    @Test
    public void shouldUpdatePleaSuccessfully() throws Exception {
        final UUID userId = UUID.randomUUID();
        final Envelope<UpdateOffencePleas> envelope = updateOffencePleasEnvelope(userId);

        defenceClientAggregate.receiveADefenceClient(null, null, uk.gov.justice.cps.defence.DefendantDetails.defendantDetails().withCaseId(CASE_ID).build(), null, null);

        defencePleaCommandHandler.updatePlea(envelope);
        final Stream<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(eventStream);
        assertThat(events, streamContaining(
                        jsonEnvelope(
                                metadata()
                                        .withName("defence.event.allocation-pleas-updated"),
                                JsonEnvelopePayloadMatcher.payload().isJson(allOf(
                                        withJsonPath("$.pleasAllocation", notNullValue()),
                                        withJsonPath("$.pleasAllocation.caseId", is(CASE_ID.toString())),
                                        withJsonPath("$.pleasAllocation.offencePleas[0].pleaDate", is(LocalDate.now().toString())),
                                        withJsonPath("$.pleasAllocation.defendantNameDobConfirmation", is(false)),
                                        withJsonPath("$.pleasAllocation.defendantDetails.firstName", is("Lee")),
                                        withJsonPath("$.pleasAllocation.defendantDetails.surname", is("Brown")),
                                        withJsonPath("$.pleasAllocation.defendantDetails.dob", is(LocalDate.now().toString()))
                                )))
                )
        );
    }

    private Envelope<AddOffencePleas> createAddOffencePleasEnvelope(final UUID userId) {
        final List<OffencePleaDetails> offencePleaDetails = new ArrayList<>();
        offencePleaDetails.add(OffencePleaDetails.offencePleaDetails()
                .withOffenceId(UUID.randomUUID())
                .withPleaDate(LocalDate.now()).build()
        );

        final AddOffencePleas addOffencePleas = AddOffencePleas.addOffencePleas()
                .withPleasAllocation(
                        PleasAllocationDetails.pleasAllocationDetails()
                                .withOffencePleas(offencePleaDetails)
                                .build())
                .build();
        final Metadata metadata = Envelope
                .metadataBuilder()
                .withName(COMMAND_HANDLER_OFFENCE_PLEAS_ADD)
                .withId(randomUUID())
                .withUserId(userId.toString())
                .build();
        return envelopeFrom(metadata, addOffencePleas);
    }

    private Envelope<UpdateOffencePleas> updateOffencePleasEnvelope(final UUID userId) {
        final List<OffencePleaDetails> offencePleaDetails = new ArrayList<>();
        offencePleaDetails.add(OffencePleaDetails.offencePleaDetails()
                .withOffenceId(randomUUID())
                .withPleaDate(LocalDate.now()).build()
        );
        final UpdateOffencePleas updateOffencePleas = UpdateOffencePleas.updateOffencePleas()
                .withPleasAllocation(
                        PleasAllocationDetails.pleasAllocationDetails()
                                .withOffencePleas(offencePleaDetails)
                                .withCrownCourtObjection(YesNoNa.NA)
                                .withDefendantNameDobConfirmation(false)
                                .withDefendantDetails(PleaDefendantDetails.pleaDefendantDetails()
                                        .withFirstName("Lee")
                                        .withSurname("Brown")
                                        .withDob(LocalDate.now())
                                        .build())
                                .build())
                .build();
        final Metadata metadata = Envelope
                .metadataBuilder()
                .withName(COMMAND_HANDLER_OFFENCE_PLEAS_UPDATE)
                .withId(randomUUID())
                .withUserId(userId.toString())
                .build();
        return envelopeFrom(metadata, updateOffencePleas);
    }
}