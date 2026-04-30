package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefenceAssociation;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class DefenceAssociationRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public DefenceAssociation findBy(final UUID id) {
        return entityManager.find(DefenceAssociation.class, id);
    }

    public List<DefenceAssociation> findByLAAContractNumber(final Collection<String> laaContractNumbers) {
        return entityManager.createQuery(
                        "SELECT entity FROM DefenceAssociation entity where entity.laaContractNumber in (:laaContractNumbers) and entity.endDate is null",
                        DefenceAssociation.class)
                .setParameter("laaContractNumbers", laaContractNumbers)
                .getResultList();
    }

    public List<DefenceAssociation> findByOrganisationIdAndCaseId(final UUID organisationId, final UUID caseId) {
        return entityManager.createQuery(
                        "select da from DefenceAssociation da, DefenceClient dc where da.orgId = :organisationId and da.defenceAssociationDefendant.defendantId=dc.defendantId and dc.caseId = :caseId",
                        DefenceAssociation.class)
                .setParameter("organisationId", organisationId)
                .setParameter("caseId", caseId)
                .getResultList();
    }

    public List<DefenceAssociation> findByUserIdAndCurrentDate(final UUID userId, final ZonedDateTime currentDate) {
        return entityManager.createQuery(
                        "SELECT da FROM DefenceAssociation da WHERE da.userId = :userId AND (da.endDate is null OR da.startDate <= :currentDate AND da.endDate >= :currentDate)",
                        DefenceAssociation.class)
                .setParameter("userId", userId)
                .setParameter("currentDate", currentDate)
                .getResultList();
    }

    public DefenceAssociation save(final DefenceAssociation entity) {
        return entityManager.merge(entity);
    }

    public void remove(final DefenceAssociation entity) {
        final DefenceAssociation managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
