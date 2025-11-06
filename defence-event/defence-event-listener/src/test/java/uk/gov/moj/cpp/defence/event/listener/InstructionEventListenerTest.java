package uk.gov.moj.cpp.defence.event.listener;

import static java.time.LocalDate.parse;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.defence.events.InstructionDetailsRecorded;
import uk.gov.moj.cpp.defence.persistence.DefenceClientRepository;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;
import uk.gov.moj.cpp.defence.persistence.entity.Instruction;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InstructionEventListenerTest {

    public static final UUID DEFENCE_CLIENT_ID = randomUUID();


    @Mock
    private DefenceClientRepository defenceClientRepository;

    @Mock
    private Envelope<InstructionDetailsRecorded> envelope;

    @InjectMocks
    private InstructionEventListener instructionEventListener;

    @Test
    public void shouldAttachAllegationToDefenceClientEntity() {

        final DefenceClient defClient = new DefenceClient(DEFENCE_CLIENT_ID, "FIRST NAME", "LAST NAME", randomUUID(), LocalDate.of(1970, 5, 17), randomUUID());
        final LocalDate instructionDate = parse("2018-01-01");
        final InstructionDetailsRecorded instructionDetailsRecorded = InstructionDetailsRecorded.instructionDetailsRecorded()
                .withUserId(randomUUID())
                .withDefenceClientId(DEFENCE_CLIENT_ID)
                .withInstructionDate(instructionDate)
                .withOrganisationId(randomUUID())
                .build();
        when(envelope.payload()).thenReturn(instructionDetailsRecorded);
        when(defenceClientRepository.findOptionalBy(any())).thenReturn(of(defClient));

        instructionEventListener.recordInstructionDetails(envelope);

        assertThat(defClient.getInstructionHistory().size(), is(1));
        final Instruction instruction = defClient.getInstructionHistory().get(0);
        assertThat(instruction.getInstructionDate(), is(instructionDate));
        assertThat(instruction.getUserId(), is(instructionDetailsRecorded.getUserId()));
        assertThat(instruction.getOrganisationId(), is(instructionDetailsRecorded.getOrganisationId()));
        assertThat(instruction.getId(), is(instructionDetailsRecorded.getInstructionId()));
        assertThat(instruction.getDefenceClient().getId(), is(DEFENCE_CLIENT_ID));

    }
}
