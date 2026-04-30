package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.IdpcAccess;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class IdpcAccessHistoryRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public IdpcAccess findBy(final UUID id) {
        return entityManager.find(IdpcAccess.class, id);
    }

    public List<IdpcAccess> findIdpcAccessByCriteria(final UUID defenceClientId) {
        return entityManager.createQuery(
                        "SELECT ia FROM IdpcAccess ia WHERE ia.defenceClientId = :defenceClientId",
                        IdpcAccess.class)
                .setParameter("defenceClientId", defenceClientId)
                .getResultList();
    }

    public List<IdpcAccess> findIdpcAccessByCriteria(final UUID defenceClientId, final UUID idpcId) {
        return entityManager.createQuery(
                        "SELECT ia FROM IdpcAccess ia WHERE ia.defenceClientId = :defenceClientId and ia.idpcId = :idpcId",
                        IdpcAccess.class)
                .setParameter("defenceClientId", defenceClientId)
                .setParameter("idpcId", idpcId)
                .getResultList();
    }

    public List<UUID> findIdpcAccessOrganisationByCriteria(final UUID defenceClientId, final UUID idpcId) {
        return entityManager.createQuery(
                        "Select ia.organisationId FROM IdpcAccess ia WHERE ia.defenceClientId = :defenceClientId and ia.idpcId = :idpcId",
                        UUID.class)
                .setParameter("defenceClientId", defenceClientId)
                .setParameter("idpcId", idpcId)
                .getResultList();
    }

    public List<UUID> findOrderedDistinctOrgIdsOfIdpcAccessForDefenceClient(final UUID defenceClientId) {
        return entityManager.createQuery(
                        "Select ia.organisationId FROM IdpcAccess ia WHERE ia.defenceClientId = :defenceClientId GROUP BY ia.organisationId ORDER BY max(accessTimestamp) DESC",
                        UUID.class)
                .setParameter("defenceClientId", defenceClientId)
                .getResultList();
    }

    public IdpcAccess save(final IdpcAccess entity) {
        return entityManager.merge(entity);
    }

    public void remove(final IdpcAccess entity) {
        final IdpcAccess managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
