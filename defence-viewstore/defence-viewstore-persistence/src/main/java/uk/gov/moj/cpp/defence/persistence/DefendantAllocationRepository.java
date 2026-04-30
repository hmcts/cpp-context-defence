package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefendantAllocation;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class DefendantAllocationRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public DefendantAllocation findBy(final UUID id) {
        return entityManager.find(DefendantAllocation.class, id);
    }

    public List<DefendantAllocation> findDefendantAllocationByCaseId(final UUID caseId) {
        return entityManager.createQuery(
                        "SELECT DISTINCT p FROM DefendantAllocation p LEFT JOIN FETCH p.defendantAllocationPleas where p.defendantId in (select dc.defendantId from DefenceClient dc where dc.caseId = :caseId)",
                        DefendantAllocation.class)
                .setParameter("caseId", caseId)
                .getResultList();
    }

    public DefendantAllocation save(final DefendantAllocation entity) {
        return entityManager.merge(entity);
    }

    public void remove(final DefendantAllocation entity) {
        final DefendantAllocation managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
