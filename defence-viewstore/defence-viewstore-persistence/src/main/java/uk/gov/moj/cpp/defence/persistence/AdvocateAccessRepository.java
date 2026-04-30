package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionAdvocateAccess;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class AdvocateAccessRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public ProsecutionAdvocateAccess findBy(final UUID id) {
        return entityManager.find(ProsecutionAdvocateAccess.class, id);
    }

    public List<ProsecutionAdvocateAccess> findByCaseIdAndAssigneeId(final UUID caseId, final UUID assigneeId) {
        return entityManager.createQuery(
                        "select entity from ProsecutionAdvocateAccess entity where entity.prosecutionOrganisation.id.caseId = :caseId and entity.assigneeDetails.userId = :assigneeId",
                        ProsecutionAdvocateAccess.class)
                .setParameter("caseId", caseId)
                .setParameter("assigneeId", assigneeId)
                .getResultList();
    }

    public List<ProsecutionAdvocateAccess> findActiveByCaseIdAndAssigneeId(final UUID caseId, final UUID assigneeId) {
        return entityManager.createQuery(
                        "select entity from ProsecutionAdvocateAccess entity where entity.prosecutionOrganisation.id.caseId = :caseId and entity.assigneeDetails.userId = :assigneeId and (entity.assignmentExpiryDate is null or entity.assignmentExpiryDate > CURRENT_TIMESTAMP)",
                        ProsecutionAdvocateAccess.class)
                .setParameter("caseId", caseId)
                .setParameter("assigneeId", assigneeId)
                .getResultList();
    }

    public List<ProsecutionAdvocateAccess> findExpiredCaseAssignments() {
        return entityManager.createQuery(
                        "select entity from ProsecutionAdvocateAccess entity where entity.assignmentExpiryDate < CURRENT_TIMESTAMP order by entity.assignmentExpiryDate desc",
                        ProsecutionAdvocateAccess.class)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<ProsecutionAdvocateAccess> findExpiredCaseAssignments(final int limitCount) {
        return entityManager.createNativeQuery(
                        "select pa.* from prosecution_advocate_access pa JOIN prosecution_organisation_access po ON pa.assignee_organisation_id = po.assignee_organisation_id AND pa.case_id = po.case_id where pa.assignment_expiry_date < now() order by pa.assignment_expiry_date desc",
                        ProsecutionAdvocateAccess.class)
                .setMaxResults(limitCount)
                .getResultList();
    }

    public ProsecutionAdvocateAccess save(final ProsecutionAdvocateAccess entity) {
        return entityManager.merge(entity);
    }

    @SuppressWarnings("unchecked")
    public List<ProsecutionAdvocateAccess> findAll() {
        return entityManager.createQuery("SELECT entity FROM ProsecutionAdvocateAccess entity").getResultList();
    }

    public void remove(final ProsecutionAdvocateAccess entity) {
        final ProsecutionAdvocateAccess managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
