package uk.gov.moj.cpp.defence.persistence;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.defence.builder.DefenceClientBuilder.createDefenceClient;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalJunit4Test;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;
import uk.gov.moj.cpp.defence.persistence.entity.Instruction;

import java.util.List;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class InstructionRepositoryIT extends BaseTransactionalJunit4Test {

    @Inject
    InstructionRepository instructionRepository;

    @Inject
    DefenceClientRepository defenceClientRepository;

    @Test
    public void findAllegationsByDefenceClientId() {

        final DefenceClient defClient = createDefenceClient();

        Instruction instruction = createInstruction(defClient);
        defClient.getInstructionHistory().add(instruction);
        defenceClientRepository.save(defClient);

        final List<Instruction> instructionHistory = instructionRepository.findInstructionsByCriteria(defClient.getId(), instruction.getUserId());

        assertThat(instructionHistory.size(), is(1));
        final Instruction savedInstruction = instructionHistory.get(0);

        assertThat(savedInstruction.getDefenceClient().getId(), is(defClient.getId()));
        assertThat(savedInstruction.getId(), is(instruction.getId()));
        assertThat(savedInstruction.getUserId(), is(instruction.getUserId()));
        assertThat(savedInstruction.getOrganisationId(), is(instruction.getOrganisationId()));
        assertThat(savedInstruction.getInstructionDate(), is(instruction.getInstructionDate()));
    }


    private Instruction createInstruction(final DefenceClient defClient) {
        return new Instruction(randomUUID(), randomUUID(), randomUUID(), defClient, now());
    }
}