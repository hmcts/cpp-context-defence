package uk.gov.moj.cpp.defence.command.api;

import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import uk.gov.justice.cps.defence.DefenceInstruction;
import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefenceClientCommandApiTest {

    public static final UUID DEFENCE_CLIENT_ID = randomUUID();
    public static final String INSTRUCTION_DATE = "2018-03-01";
    @Mock
    private Sender sender;

    @InjectMocks
    private DefenceClientCommandApi defenceClientCommandApi;

    @Captor
    ArgumentCaptor<Envelope<DefenceInstruction>> envelopeArgumentCaptor;

    @Test
    public void shouldHandleRecordInstructionDate() {
        defenceClientCommandApi.recordInstructionDetails(createEnvelop(INSTRUCTION_DATE));

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<DefenceInstruction> defenceInstructionEnvelope = envelopeArgumentCaptor.getValue();
        assertThat(defenceInstructionEnvelope.metadata().name(), is("defence.command.record-instruction-details"));
        assertThat(defenceInstructionEnvelope.payload().getDefenceClientId(), is(DEFENCE_CLIENT_ID));
        assertThat(defenceInstructionEnvelope.payload().getInstructionDate(), is(INSTRUCTION_DATE));
    }


    @Test
    public void shouldThrowBadRequestExceptionWhenDateIsInFuture() {

        final String futureDate = LocalDate.now().plusDays(1).toString();

        var e = assertThrows(BadRequestException.class, () -> defenceClientCommandApi.recordInstructionDetails(createEnvelop(futureDate)));
        assertThat(e.getMessage(), is(format("Date is in future. Date : %s", futureDate)));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenDateIsInvalid() {

        final String invalidDateString = "xx-xx-xx";

        var e = assertThrows(BadRequestException.class, () -> defenceClientCommandApi.recordInstructionDetails(createEnvelop(invalidDateString)));
        assertThat(e.getMessage(), is(format("Invalid date format. Input date string: %s", invalidDateString)));
    }

    private Envelope<DefenceInstruction> createEnvelop(final String instructionDate) {
        final DefenceInstruction recordInstrcutionDetailsCommand = new DefenceInstruction(DEFENCE_CLIENT_ID, instructionDate);
        final Metadata metadata = Envelope.metadataBuilder().withId(randomUUID())
                .withName("defence.record-instruction-details")
                .createdAt(now()).build();
        return Envelope.envelopeFrom(metadata, recordInstrcutionDetailsCommand);
    }

}
