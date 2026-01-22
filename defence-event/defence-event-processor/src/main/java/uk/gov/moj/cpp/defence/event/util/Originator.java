package uk.gov.moj.cpp.defence.event.util;

import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.services.messaging.JsonMetadata.ID;
import static uk.gov.justice.services.messaging.JsonMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonMetadata.USER_ID;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.JsonObjects;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.UUID;

public class Originator {

    public static final String CONTEXT = "context";
    public static final String SOURCE = "originator";
    public static final String ORIGINATOR_VALUE = "court";

    private Originator() {
    }

    public static Metadata createMetadataWithProcessIdAndUserId(final String id, final String name, final String userId) {
        return metadataFrom(JsonObjects.createObjectBuilder()
                .add(ID, id)
                .add(NAME, name)
                .add(SOURCE, ORIGINATOR_VALUE)
                .add(CONTEXT, JsonObjects.createObjectBuilder()
                        .add(USER_ID, userId))
                .build()).build();
    }

    public static JsonEnvelope assembleEnvelopeWithPayloadAndMetaDetails(final JsonObject payload, final String contentType, final String userId) {
        final Metadata metadata = createMetadataWithProcessIdAndUserId(UUID.randomUUID().toString(), contentType, userId);
        final JsonObject payloadWithMetada = addMetadataToPayload(payload, metadata);
        return envelopeFrom(metadata, payloadWithMetada);
    }

    private static JsonObject addMetadataToPayload(final JsonObject load, final Metadata metadata) {
        final JsonObjectBuilder job = JsonObjects.createObjectBuilder();
        load.entrySet().forEach(entry -> job.add(entry.getKey(), entry.getValue()));
        job.add(JsonEnvelope.METADATA, metadata.asJsonObject());
        return job.build();
    }
}

