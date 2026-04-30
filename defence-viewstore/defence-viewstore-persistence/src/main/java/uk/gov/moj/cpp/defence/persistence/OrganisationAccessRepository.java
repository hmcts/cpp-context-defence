package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionOrganisationAccess;
import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionOrganisationCaseKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class OrganisationAccessRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public ProsecutionOrganisationAccess findBy(final ProsecutionOrganisationCaseKey id) {
        return entityManager.find(ProsecutionOrganisationAccess.class, id);
    }

    public List<ProsecutionOrganisationAccess> findByCaseId(final UUID caseId) {
        return entityManager.createQuery(
                        "SELECT poa FROM ProsecutionOrganisationAccess poa where poa.id.caseId = :caseId",
                        ProsecutionOrganisationAccess.class)
                .setParameter("caseId", caseId)
                .getResultList();
    }

    public Optional<ProsecutionOrganisationAccess> findByAssigneeOrganisationIdAndCaseId(
            final UUID assigneeOrganisationId, final UUID caseId) {
        return entityManager.createQuery(
                        "SELECT poa FROM ProsecutionOrganisationAccess poa where poa.id.assigneeOrganisationId = :assigneeOrganisationId and poa.id.caseId = :caseId",
                        ProsecutionOrganisationAccess.class)
                .setParameter("assigneeOrganisationId", assigneeOrganisationId)
                .setParameter("caseId", caseId)
                .getResultStream().findFirst();
    }

    public List<ProsecutionOrganisationAccess> findByCaseIdAndAssigneeOrganisationId(final UUID caseId,
            final UUID assigneeOrganisationId) {
        return entityManager.createQuery(
                        "SELECT poa FROM ProsecutionOrganisationAccess poa where poa.id.caseId = :caseId and poa.id.assigneeOrganisationId = :assigneeOrganisationId",
                        ProsecutionOrganisationAccess.class)
                .setParameter("caseId", caseId)
                .setParameter("assigneeOrganisationId", assigneeOrganisationId)
                .getResultList();
    }

    public List<ProsecutionOrganisationAccess> findActiveByCaseIdAndAssigneeOrganisationId(final UUID caseId,
            final UUID assigneeOrganisationId) {
        return entityManager.createQuery(
                        "SELECT poa FROM ProsecutionOrganisationAccess poa where poa.id.caseId = :caseId and poa.id.assigneeOrganisationId = :assigneeOrganisationId and (poa.assignmentExpiryDate is null or poa.assignmentExpiryDate > CURRENT_TIMESTAMP)",
                        ProsecutionOrganisationAccess.class)
                .setParameter("caseId", caseId)
                .setParameter("assigneeOrganisationId", assigneeOrganisationId)
                .getResultList();
    }

    public List<ProsecutionOrganisationAccess> findExpiredCaseAssignments() {
        return entityManager.createQuery(
                        "SELECT poa FROM ProsecutionOrganisationAccess poa where poa.assignmentExpiryDate < CURRENT_TIMESTAMP and poa.prosecutionAdvocatesWithAccess is EMPTY order by poa.assignmentExpiryDate desc",
                        ProsecutionOrganisationAccess.class)
                .getResultList();
    }

    public List<ProsecutionOrganisationAccess> findExpiredCaseAssignments(final int max) {
        return entityManager.createQuery(
                        "SELECT poa FROM ProsecutionOrganisationAccess poa where poa.assignmentExpiryDate < CURRENT_TIMESTAMP and poa.prosecutionAdvocatesWithAccess is EMPTY order by poa.assignmentExpiryDate desc",
                        ProsecutionOrganisationAccess.class)
                .setMaxResults(max)
                .getResultList();
    }

    public ProsecutionOrganisationAccess save(final ProsecutionOrganisationAccess entity) {
        return entityManager.merge(entity);
    }

    @SuppressWarnings("unchecked")
    public List<ProsecutionOrganisationAccess> findAll() {
        return entityManager.createQuery("SELECT entity FROM ProsecutionOrganisationAccess entity").getResultList();
    }

    public void flush() {
        entityManager.flush();
    }

    public ProsecutionOrganisationAccess saveAndFlush(final ProsecutionOrganisationAccess entity) {
        final ProsecutionOrganisationAccess saved = entityManager.merge(entity);
        entityManager.flush();
        return saved;
    }

    public void remove(final ProsecutionOrganisationAccess entity) {
        final ProsecutionOrganisationAccess managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
