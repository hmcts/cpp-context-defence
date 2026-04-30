package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.IdpcDetails;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class IdpcDetailsRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public IdpcDetails findBy(final UUID id) {
        return entityManager.find(IdpcDetails.class, id);
    }

    public IdpcDetails findIdpcDetailsForDefenceClient(final UUID defenceClientId) {
        return entityManager.createQuery(
                        "SELECT idpc FROM IdpcDetails idpc WHERE idpc.defenceClientId = :defenceClientId",
                        IdpcDetails.class)
                .setParameter("defenceClientId", defenceClientId)
                .getResultStream().findFirst().orElse(null);
    }

    public IdpcDetails findOptionalByDefenceClientId(final UUID defenceClientId) {
        return entityManager.createQuery(
                        "SELECT idpc FROM IdpcDetails idpc WHERE idpc.defenceClientId = :defenceClientId",
                        IdpcDetails.class)
                .setParameter("defenceClientId", defenceClientId)
                .getResultStream().findFirst().orElse(null);
    }

    public IdpcDetails findIdpcDetailsForDefendantId(final UUID defendantId) {
        return entityManager.createQuery(
                        "select idpc FROM IdpcDetails idpc where idpc.defenceClientId in (select dc.id from DefenceClient dc where defendantId = :defendantId)",
                        IdpcDetails.class)
                .setParameter("defendantId", defendantId)
                .getResultStream().findFirst().orElse(null);
    }

    public IdpcDetails save(final IdpcDetails entity) {
        return entityManager.merge(entity);
    }

    public void remove(final IdpcDetails entity) {
        final IdpcDetails managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
