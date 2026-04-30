package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefenceGrantAccess;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class DefenceGrantAccessRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public DefenceGrantAccess findBy(final UUID id) {
        return entityManager.find(DefenceGrantAccess.class, id);
    }

    public List<DefenceGrantAccess> findByDefenceClient(final UUID defendantClientId) {
        return entityManager.createQuery(
                        "select dga FROM DefenceGrantAccess dga WHERE dga.defenceClient.id = :defendantClientId and dga.removed = false",
                        DefenceGrantAccess.class)
                .setParameter("defendantClientId", defendantClientId)
                .getResultList();
    }

    public DefenceGrantAccess findByDefenceClient(final UUID defendantClientId, final UUID userId) {
        return entityManager.createQuery(
                        "select dga FROM DefenceGrantAccess dga WHERE dga.defenceClient.id = :defendantClientId and dga.granteeDefenceUserDetails.userId = :userId and dga.removed = false",
                        DefenceGrantAccess.class)
                .setParameter("defendantClientId", defendantClientId)
                .setParameter("userId", userId)
                .getResultStream().findFirst().orElse(null);
    }

    public List<DefenceGrantAccess> findByGranteeAndCaseId(final UUID caseId, final UUID userId) {
        return entityManager.createQuery(
                        "select dga FROM DefenceGrantAccess dga WHERE dga.defenceClient.caseId = :caseId and dga.granteeDefenceUserDetails.userId = :userId and dga.removed = false",
                        DefenceGrantAccess.class)
                .setParameter("caseId", caseId)
                .setParameter("userId", userId)
                .getResultList();
    }

    public DefenceGrantAccess save(final DefenceGrantAccess entity) {
        return entityManager.merge(entity);
    }

    public void remove(final DefenceGrantAccess entity) {
        final DefenceGrantAccess managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
