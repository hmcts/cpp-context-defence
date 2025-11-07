package uk.gov.moj.cpp.defence.command.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.defence.query.view.DefenceGrantAccessQueryView;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefenceServiceTest {

    public static final String GRANTEES = "grantees";
    @InjectMocks
    private DefenceService defenceService;

    @Mock
    private DefenceGrantAccessQueryView defenceGrantAccessQueryView;

    @Test
    public void shouldReturnTrueForIsAssigneeDefendingTheCaseWhenGranteeIsReturned() {
        final UUID uuid = randomUUID();
        final UUID userId = randomUUID();
        final UUID caseId = randomUUID();
        final UUID granteeUserId = randomUUID();
        final Metadata metadata = getMetaData(uuid, userId);

        when(defenceGrantAccessQueryView.getCaseGrantee(any(JsonEnvelope.class))).thenAnswer(invocationOnMock -> {
            final JsonEnvelope envelope = (JsonEnvelope) invocationOnMock.getArguments()[0];
            final JsonObject responsePayload = Json.createObjectBuilder()
                    .add(GRANTEES, Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                    .build())
                            .build())
                    .build();
            return JsonEnvelope.envelopeFrom(envelope.metadata(), responsePayload);
        });

        final boolean result = defenceService.isAssigneeDefendingTheCase(metadata, caseId, granteeUserId);

        assertThat(result, is(true));
    }

    @Test
    public void shouldReturnTrueForIsAssigneeDefendingTheCaseWhenGranteeIsNull() {
        final UUID uuid = randomUUID();
        final UUID userId = randomUUID();
        final UUID caseId = randomUUID();
        final UUID granteeUserId = randomUUID();
        final Metadata metadata = getMetaData(uuid, userId);

        when(defenceGrantAccessQueryView.getCaseGrantee(any(JsonEnvelope.class))).thenAnswer(invocationOnMock -> {
            final JsonEnvelope envelope = (JsonEnvelope) invocationOnMock.getArguments()[0];
            final JsonObject responsePayload = Json.createObjectBuilder()
                    .build();
            return JsonEnvelope.envelopeFrom(envelope.metadata(), responsePayload);
        });

        final boolean result = defenceService.isAssigneeDefendingTheCase(metadata, caseId, granteeUserId);

        assertThat(result, is(false));
    }

    @Test
    public void shouldReturnTrueForIsAssigneeDefendingTheCaseWhenGranteeIsEmpty() {
        final UUID uuid = randomUUID();
        final UUID userId = randomUUID();
        final UUID caseId = randomUUID();
        final UUID granteeUserId = randomUUID();
        final Metadata metadata = getMetaData(uuid, userId);

        when(defenceGrantAccessQueryView.getCaseGrantee(any(JsonEnvelope.class))).thenAnswer(invocationOnMock -> {
            final JsonEnvelope envelope = (JsonEnvelope) invocationOnMock.getArguments()[0];
            final JsonObject responsePayload = Json.createObjectBuilder()
                    .add(GRANTEES, Json.createArrayBuilder()
                            .build())
                    .build();
            return JsonEnvelope.envelopeFrom(envelope.metadata(), responsePayload);
        });

        final boolean result = defenceService.isAssigneeDefendingTheCase(metadata, caseId, granteeUserId);

        assertThat(result, is(false));
    }

    private Metadata getMetaData(final UUID uuid, final UUID userId) {
        return Envelope
                .metadataBuilder()
                .withName("anyEventName")
                .withId(uuid)
                .withUserId(userId.toString())
                .build();
    }

}
