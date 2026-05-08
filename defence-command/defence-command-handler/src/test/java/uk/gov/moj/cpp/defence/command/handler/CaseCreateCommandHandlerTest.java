package uk.gov.moj.cpp.defence.command.handler;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.justice.cps.defence.AssociateOrganisationBdf;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;
import uk.gov.moj.cpp.defence.aggregate.CaseDefenceClientMap;
import uk.gov.moj.cpp.defence.events.CaseCreatedBdf;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationAssociatedBdf;
import uk.gov.moj.cpp.progression.json.schema.event.ProsecutionCaseCreatedBdf;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class CaseCreateCommandHandlerTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter(objectMapper);

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter(objectMapper);

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream eventStream;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private final Enveloper enveloper = EnveloperFactory.createEnveloperWithEvents(CaseCreatedBdf.class, DefenceOrganisationAssociatedBdf.class);

    @InjectMocks
    private CaseCreateCommandHandler caseCreateCommandHandler;

    @Test
    void shouldProcessCommandHandler() throws EventStreamException {

        final Metadata metadata =  Envelope
                .metadataBuilder()
                .withName("defence.command.prosecution-case-created-bdf")
                .withId(randomUUID())
                .withUserId(randomUUID().toString())
                .build();

        final Envelope<ProsecutionCaseCreatedBdf> envelope = Envelope.envelopeFrom(metadata, new ProsecutionCaseCreatedBdf(randomUUID(), randomUUID()));
        final var aggregate = new CaseDefenceClientMap();
        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, CaseDefenceClientMap.class)).thenReturn(aggregate);

        caseCreateCommandHandler.caseCreatedBdf(envelope);

        final List<JsonEnvelope> envelopeStream = verifyAppendAndGetArgumentFrom(eventStream).toList();

        assertThat(envelopeStream.isEmpty(), is(false));
        assertThat(envelopeStream.get(0).metadata().name(), is("defence.event.case-created-bdf"));

    }

    @Test
    void shouldProcessAssociateOrganisationBdf() throws EventStreamException {

        final Metadata metadata =  Envelope
                .metadataBuilder()
                .withName("defence.command.associate-organisation-bdf")
                .withId(randomUUID())
                .withUserId(randomUUID().toString())
                .build();

        final Envelope<AssociateOrganisationBdf> envelope = Envelope.envelopeFrom(metadata, AssociateOrganisationBdf.associateOrganisationBdf().withDefendantId(randomUUID()).build());
        when(eventSource.getStreamById(any())).thenReturn(eventStream);

        caseCreateCommandHandler.handleAssociateOrganisationBdf(envelope);

        final List<JsonEnvelope> envelopeStream = verifyAppendAndGetArgumentFrom(eventStream).toList();

        assertThat(envelopeStream.isEmpty(), is(false));
        assertThat(envelopeStream.get(0).metadata().name(), is("defence.event.defence-organisation-associated-bdf"));

    }
}
