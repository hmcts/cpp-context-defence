package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefendantAllocationPlea;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class DefendantAllocationPleaRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public DefendantAllocationPlea findBy(final UUID id) {
        return entityManager.find(DefendantAllocationPlea.class, id);
    }

    public DefendantAllocationPlea save(final DefendantAllocationPlea entity) {
        return entityManager.merge(entity);
    }

    public void remove(final DefendantAllocationPlea entity) {
        final DefendantAllocationPlea managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
