package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefenceAssociationDefendant;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class DefenceAssociationDefendantRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public DefenceAssociationDefendant findBy(final UUID id) {
        return entityManager.find(DefenceAssociationDefendant.class, id);
    }

    public DefenceAssociationDefendant findOptionalByDefendantId(final UUID defendantId) {
        return entityManager.createQuery(
                        "SELECT d FROM DefenceAssociationDefendant d WHERE d.defendantId = :defendantId",
                        DefenceAssociationDefendant.class)
                .setParameter("defendantId", defendantId)
                .getResultStream().findFirst().orElse(null);
    }

    public DefenceAssociationDefendant save(final DefenceAssociationDefendant entity) {
        return entityManager.merge(entity);
    }

    public void remove(final DefenceAssociationDefendant entity) {
        final DefenceAssociationDefendant managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
