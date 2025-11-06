package uk.gov.moj.cpp.defence.event.helper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;

import java.util.List;

public class EnvelopeHelper {

    public static <T> Envelope<T> getEnvelope(final T payload, final String eventName) {
        return Envelope.envelopeFrom(
                metadataWithRandomUUID(eventName),
                payload
        );
    }

    public static void verifySendAtIndex(final List<Envelope<?>> messageEnvelope,
                                         final String commandOrEventName, final int index) {
        final DefaultEnvelope argumentCaptorValue = (DefaultEnvelope) messageEnvelope.get(index);
        assertThat(argumentCaptorValue.metadata().name(), is(commandOrEventName));
    }
}
