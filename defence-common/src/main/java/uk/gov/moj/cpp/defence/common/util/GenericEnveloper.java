package uk.gov.moj.cpp.defence.common.util;

import static uk.gov.justice.services.messaging.Envelope.metadataFrom;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

public class GenericEnveloper {

    public GenericEnveloper() {
        /* helper class with no properties  */
    }

    public <T> Envelope<T> envelopeWithNewActionName(final T payload, final Metadata metadata, final String actionName) {
        return Envelope.envelopeFrom(metadataFrom(metadata).withName(actionName), payload);
    }


}