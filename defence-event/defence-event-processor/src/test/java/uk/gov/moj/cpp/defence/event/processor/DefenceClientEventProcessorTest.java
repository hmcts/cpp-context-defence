package uk.gov.moj.cpp.defence.event.processor;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.moj.cpp.progression.json.schema.event.PublicDefendantIdpcAdded.publicDefendantIdpcAdded;

import uk.gov.justice.cps.defence.DefenceClientDetails;
import uk.gov.justice.cps.defence.DefenceClientMappedToACase;
import uk.gov.justice.cps.defence.DefendantAdded;
import uk.gov.justice.cps.defence.DefendantDetails;
import uk.gov.justice.cps.defence.Offence;
import uk.gov.justice.cps.defence.ReceiveAllegationsAgainstADefenceClient;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;
import uk.gov.moj.cpp.defence.IdpcDetails;
import uk.gov.moj.cpp.defence.common.util.GenericEnveloper;
import uk.gov.moj.cpp.defence.event.processor.commands.ReceiveDefenceClient;
import uk.gov.moj.cpp.defence.event.processor.commands.RecordIdpcDetails;
import uk.gov.moj.cpp.progression.json.schema.event.PublicDefendantIdpcAdded;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.json.Json;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
public class DefenceClientEventProcessorTest {

    private static final UUID DEFENCE_CLIENT_ID = randomUUID();
    private static final UUID PERSON_ID = randomUUID();
    private static final String URN = "55DP0028116";
    private static final String DOB = "1960-04-01";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final UUID DEFENDANT_ID = randomUUID();
    @Spy
    private final Enveloper enveloper = EnveloperFactory.createEnveloper();
    @Captor
    ArgumentCaptor<Envelope<?>> envelopeArgumentCaptor;
    @Mock
    private Sender sender;
    @Mock
    private GenericEnveloper mockGenericEnveloper;

    @InjectMocks
    private DefenceClientEventProcessor defenceClientEventProcessor;

    @Test
    public void shouldSendCreateDefenceClientCommandMessage() throws Exception {

        final Metadata metadata = mock(Metadata.class);
        final Envelope<DefenceClientMappedToACase> envelopeDcmtac = mock(Envelope.class);
        final DefenceClientMappedToACase defenceClientMappedToACase = mock(DefenceClientMappedToACase.class);
        final DefenceClientDetails defenceClientDetails = mock(DefenceClientDetails.class);
        final DefendantDetails defendantDetails = mock(DefendantDetails.class);

        when(envelopeDcmtac.payload()).thenReturn(defenceClientMappedToACase);
        when(envelopeDcmtac.metadata()).thenReturn(metadata);
        when(defenceClientMappedToACase.getDefenceClientId()).thenReturn(DEFENCE_CLIENT_ID);
        when(defenceClientMappedToACase.getDefendantDetails()).thenReturn(defendantDetails);
        when(defenceClientMappedToACase.getDefenceClientDetails()).thenReturn(defenceClientDetails);
        when(defenceClientMappedToACase.getUrn()).thenReturn(URN);
        when(defenceClientMappedToACase.getDefendantId()).thenReturn(DEFENDANT_ID);

        final Envelope<ReceiveDefenceClient> receiveDefenceClientEnvelope = mock(Envelope.class);

        final Answer<Envelope<ReceiveDefenceClient>> answerReceiveDefenceClientCommamnd = invocation -> {
            final ReceiveDefenceClient receiveDefenceClient = invocation.getArgument(0, ReceiveDefenceClient.class);
            assertThat(invocation.getArgument(1, Metadata.class), is(metadata));

            final Metadata commandMetaData = mock(Metadata.class);
            final String commandName = invocation.getArgument(2, String.class);
            return receiveDefenceClientEnvelope;
        };

        when(mockGenericEnveloper.envelopeWithNewActionName(
                any(ReceiveDefenceClient.class),
                eq(metadata),
                eq("defence.command.receive-defence-client")))
                .thenAnswer(answerReceiveDefenceClientCommamnd);

        defenceClientEventProcessor.handleSuspectAdded(envelopeDcmtac);

        verify(sender, times(1)).send(receiveDefenceClientEnvelope);
        verify(defenceClientMappedToACase,times(1)).getDefendantId();
    }

    @Test
    public void shouldSendReceiveAllegationsAgainstDefenceClientCommand() {

        final Metadata metadata = mock(Metadata.class);
        final Envelope envelope = mock(Envelope.class);
        final DefendantAdded defendantAdded = mock(DefendantAdded.class);
        final DefendantDetails defendantDetails = mock(DefendantDetails.class);
        final List<Offence> offences = asList(mock(Offence.class));
        final String policeDefendantId = "policeDefendantId";

        final UUID uuid = randomUUID();

        when(envelope.payload()).thenReturn(defendantAdded);
        when(envelope.metadata()).thenReturn(metadata);
        when(defendantAdded.getDefenceClientId()).thenReturn(uuid);
        when(defendantAdded.getDefendantDetails()).thenReturn(defendantDetails);
        when(defendantAdded.getDefendantId()).thenReturn(uuid);
        when(defendantAdded.getOffences()).thenReturn(offences);
        when(defendantAdded.getPoliceDefendantId()).thenReturn(policeDefendantId);

        final Envelope<ReceiveAllegationsAgainstADefenceClient> receiveAllegationsAgainstADefenceClientEnvelope = mock(Envelope.class);

        final Answer<Envelope<ReceiveAllegationsAgainstADefenceClient>> answer = invocation -> {
            final ReceiveAllegationsAgainstADefenceClient receiveAllegationsAgainstADefenceClient = invocation.getArgument(0, ReceiveAllegationsAgainstADefenceClient.class);
            assertThat(invocation.getArgument(1, Metadata.class), is(metadata));
            final Metadata commandMetaData = mock(Metadata.class);
            return receiveAllegationsAgainstADefenceClientEnvelope;
        };

        when(mockGenericEnveloper.envelopeWithNewActionName(
                any(ReceiveAllegationsAgainstADefenceClient.class),
                eq(metadata),
                eq("defence.command.receive-allegations-against-a-defence-client")))
                .thenAnswer(answer);

        defenceClientEventProcessor.handleSuspectIsCharged(envelope);

        verify(sender, times(1)).send(receiveAllegationsAgainstADefenceClientEnvelope);
    }


    @Test
    public void shouldHandleRecordInstruction_andGenerateAPublicEvent() {
        final String privateEventName = "defence.event.instruction-details-recorded";
        final String publicEventName = "public.defence.event.record-instruction-details";

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataBuilder()
                        .withName(privateEventName)
                        .withId(UUID.randomUUID())
                        .build(),
                Json.createObjectBuilder()
                        .add("a", "a").build());

        //test
        defenceClientEventProcessor.handleRecordInstructionDetails(envelope);

        //assert
        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
        assertThat(envelopeArgumentCaptor.getValue().metadata().name(), Matchers.is(publicEventName));

    }

    @Test
    public void shouldSendIdpcPublishedForDefendantCommand() {

        final Envelope<PublicDefendantIdpcAdded> idpcPublishedForDefendantEnvelope = createIdpcPublishedForDefendantEnvelope();

        defenceClientEventProcessor.handleIdpcPublishedForDefendant(idpcPublishedForDefendantEnvelope);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

        final Envelope<RecordIdpcDetails> envelope = (Envelope<RecordIdpcDetails>) envelopeArgumentCaptor.getValue();
        final RecordIdpcDetails recordIdpcDetails = envelope.payload();
        final IdpcDetails idpcDetails = recordIdpcDetails.getIdpcDetails();

        MatcherAssert.assertThat(idpcDetails.getMaterialId(), Is.is(idpcPublishedForDefendantEnvelope.payload().getMaterialId()));
        MatcherAssert.assertThat(recordIdpcDetails.getDefenceClientId(), Is.is(idpcPublishedForDefendantEnvelope.payload().getDefendantId()));
        MatcherAssert.assertThat(idpcDetails.getPageCount(), Is.is(idpcPublishedForDefendantEnvelope.payload().getPageCount()));
        MatcherAssert.assertThat(idpcDetails.getSize(), Is.is(idpcPublishedForDefendantEnvelope.payload().getSize()));
        MatcherAssert.assertThat(idpcDetails.getPublishedDate(), Is.is(idpcPublishedForDefendantEnvelope.payload().getPublishedDate()));
        MatcherAssert.assertThat(envelope.metadata().name(), Is.is("defence.command.record-idpc-details"));
    }

    private <T> Envelope<T> createTypedEnvelope(final T t) {

        final Metadata metadata = Envelope.metadataBuilder()
                .withId(DEFENCE_CLIENT_ID)
                .withName("actionName")
                .createdAt(now())
                .build();
        return Envelope.envelopeFrom(metadata, t);
    }

    private Envelope<PublicDefendantIdpcAdded> createIdpcPublishedForDefendantEnvelope() {
        final PublicDefendantIdpcAdded defendantIdpcAdded = publicDefendantIdpcAdded()
                .withMaterialId(randomUUID())
                .withDefendantId(randomUUID())
                .withCaseId(randomUUID())
                .withPageCount(11)
                .withSize("2.8Mb")
                .withPublishedDate(LocalDate.now())
                .build();

        return createTypedEnvelope(defendantIdpcAdded);

    }
}
