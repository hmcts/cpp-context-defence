package uk.gov.moj.cpp.defence.event.processor;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;

import java.util.UUID;

import javax.json.Json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IdpcAccessRecordedEventProcessorTest {

    @Captor
    ArgumentCaptor<Envelope> envelopeArgumentCaptor;

    @Mock
    private Sender sender;

    @Spy
    private Enveloper enveloper = EnveloperFactory.createEnveloper();

    @InjectMocks
    private IdpcAccessRecordedEventProcessor idpcAccessRecordedEventProcessor;



    @Test
    public void shouldHandleRecordIdpcAccessByOrganisationMethod() {
        assertThat(new IdpcAccessRecordedEventProcessor(), isHandler(EVENT_PROCESSOR)
                .with(method("handleIdpcAccessByOrganisationRecorded")
                        .thatHandles("defence.event.idpc-access-by-organisation-recorded")
                ));
    }


    @Test
    public void shouldGenerateAnIdpcAccessedByOrganisationPublicEvent() {

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataBuilder()
                        .withName("defence.event.idpc-access-by-organisation-recorded")
                        .withId(UUID.randomUUID())
                        .build(),
                Json.createObjectBuilder()
                        .add("a", "a").build());

        //test
        idpcAccessRecordedEventProcessor.handleIdpcAccessByOrganisationRecorded(envelope);

        //assert
        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
        assertThat(envelopeArgumentCaptor.getValue().metadata().name(), is("public.defence.event.idpc-accessed-by-organisation"));
    }
}
