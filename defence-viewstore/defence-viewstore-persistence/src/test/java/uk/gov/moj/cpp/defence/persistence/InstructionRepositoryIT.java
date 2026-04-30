package uk.gov.moj.cpp.defence.persistence;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.defence.builder.DefenceClientBuilder.createDefenceClient;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;
import uk.gov.moj.cpp.defence.persistence.entity.Instruction;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class InstructionRepositoryIT {

    private static final String PERSISTENCE_UNIT = "defence-test-persistence-unit";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private InstructionRepository instructionRepository;
    private DefenceClientRepository defenceClientRepository;

    @BeforeEach
    public void setUpRepositories() {
        instructionRepository = new InstructionRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(instructionRepository);
        defenceClientRepository = new DefenceClientRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(defenceClientRepository);
    }

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
