package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefenceCase;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class DefenceCaseRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public DefenceCase findBy(final UUID id) {
        return entityManager.find(DefenceCase.class, id);
    }

    public DefenceCase findOptionalByUrn(final String urn) {
        return entityManager.createQuery(
                        "SELECT c FROM DefenceCase c WHERE c.urn = :urn", DefenceCase.class)
                .setParameter("urn", urn)
                .getResultStream().findFirst().orElse(null);
    }

    public DefenceCase save(final DefenceCase entity) {
        return entityManager.merge(entity);
    }

    public void remove(final DefenceCase entity) {
        final DefenceCase managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
