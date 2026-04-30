package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.Instruction;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class InstructionRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public Instruction findBy(final UUID id) {
        return entityManager.find(Instruction.class, id);
    }

    public List<Instruction> findInstructionsByCriteria(final UUID defenceClientId, final UUID userId) {
        return entityManager.createQuery(
                        "SELECT ins FROM Instruction ins WHERE ins.defenceClient.id = :defenceClientId and ins.userId = :userId",
                        Instruction.class)
                .setParameter("defenceClientId", defenceClientId)
                .setParameter("userId", userId)
                .getResultList();
    }

    public int findNumberOfInstructionsForUserForDefenceClient(final UUID defenceClientId, final UUID userId) {
        return ((Number) entityManager.createQuery(
                        "Select Count(ins) FROM Instruction ins WHERE ins.defenceClient.id = :defenceClientId and ins.userId = :userId")
                .setParameter("defenceClientId", defenceClientId)
                .setParameter("userId", userId)
                .getSingleResult()).intValue();
    }

    public int findNumberOfInstructionsByCriteria(final UUID defenceClientId, final UUID organisationId) {
        return ((Number) entityManager.createQuery(
                        "Select Count(ins) FROM Instruction ins WHERE ins.defenceClient.id = :defenceClientId and ins.organisationId = :organisationId")
                .setParameter("defenceClientId", defenceClientId)
                .setParameter("organisationId", organisationId)
                .getSingleResult()).intValue();
    }

    public Instruction save(final Instruction entity) {
        return entityManager.merge(entity);
    }

    public void remove(final Instruction entity) {
        final Instruction managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
