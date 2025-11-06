package uk.gov.moj.cpp.defence.command.api;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;

import uk.gov.justice.cps.defence.GrantDefenceAccess;
import uk.gov.justice.cps.defence.RemoveGrantDefenceAccess;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefenceGrantAccessApiTest {

    private static final String EMAIL = "mail@hmcts.net";
    @Mock
    private Sender sender;

    @InjectMocks
    private DefenceGrantAccessApi defenceGrantAccessApi;

    @Captor
    private ArgumentCaptor<Envelope<GrantDefenceAccess>> grantAccessEnvelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<Envelope<RemoveGrantDefenceAccess>> removeGrantAccessEnvelopeArgumentCaptor;


    @Test
    public void shouldHandleGrantDefenceAccess() {
        defenceGrantAccessApi.grantDefenceAccess(createEnvelopeForGrantAccess(EMAIL));

        verify(sender).send(grantAccessEnvelopeArgumentCaptor.capture());

        final Envelope<GrantDefenceAccess> grantDefenceAccessEnvelope = grantAccessEnvelopeArgumentCaptor.getValue();
        assertThat(grantDefenceAccessEnvelope.metadata().name(), is("defence.command.grant-defence-access"));
        assertThat(grantDefenceAccessEnvelope.payload().getGranteeEmailId(), is(EMAIL));
    }

    @Test
    public void shouldHandleRemoveGrantDefenceAccess() {
        defenceGrantAccessApi.removeGrantDefenceAccess(createEnvelopeForRemoveGrantAccess());

        verify(sender).send(removeGrantAccessEnvelopeArgumentCaptor.capture());

        final Envelope<RemoveGrantDefenceAccess> removeGrantDefenceAccessEnvelope = removeGrantAccessEnvelopeArgumentCaptor.getValue();
        assertThat(removeGrantDefenceAccessEnvelope.metadata().name(), is("defence.command.remove-grant-defence-access"));
        assertThat(removeGrantDefenceAccessEnvelope.payload().getDefenceClientId(), notNullValue());
        assertThat(removeGrantDefenceAccessEnvelope.payload().getGranteeUserId(), notNullValue());
        assertThat(removeGrantDefenceAccessEnvelope.metadata().userId(), notNullValue());

    }

    private Envelope<GrantDefenceAccess> createEnvelopeForGrantAccess(final String email) {
        final GrantDefenceAccess grantDefenceAccess = new GrantDefenceAccess(UUID.randomUUID(), email);
        final Metadata metadata = Envelope.metadataBuilder().withId(randomUUID())
                .withName("defence.grant-defence-access")
                .createdAt(now()).build();
        return Envelope.envelopeFrom(metadata, grantDefenceAccess);
    }

    private Envelope<RemoveGrantDefenceAccess> createEnvelopeForRemoveGrantAccess() {
        final RemoveGrantDefenceAccess removeGrantDefenceAccess = new RemoveGrantDefenceAccess(UUID.randomUUID(), UUID.randomUUID());
        final Metadata metadata = Envelope.metadataBuilder().withId(randomUUID())
                .withUserId(randomUUID().toString())
                .withName("defence.remove-grant-defence-access")
                .createdAt(now()).build();
        return Envelope.envelopeFrom(metadata, removeGrantDefenceAccess);
    }

}
