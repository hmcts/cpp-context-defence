package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class DefenceClientRepository {

    @PersistenceContext(unitName = "defence")
    EntityManager entityManager;

    public DefenceClient findBy(final UUID id) {
        return entityManager.find(DefenceClient.class, id);
    }

    public Optional<DefenceClient> findOptionalBy(final UUID id) {
        return Optional.ofNullable(entityManager.find(DefenceClient.class, id));
    }

    public List<DefenceClient> findDefenceClientByCriteria(final String firstName, final String lastName,
            final LocalDate dateOfBirth, final String ptiUrn) {
        return entityManager.createQuery(
                        "select dc FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and dc.dateOfBirth = :dateOfBirth and c.urn = :ptiUrn and dc.visible = true",
                        DefenceClient.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("dateOfBirth", dateOfBirth)
                .setParameter("ptiUrn", ptiUrn)
                .getResultList();
    }

    public List<DefenceClient> findDefenceClientByCriteria(final String firstName, final String lastName,
            final LocalDate dateOfBirth, final String ptiUrn, final boolean isCivil) {
        return entityManager.createQuery(
                        "select dc FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and dc.dateOfBirth = :dateOfBirth and c.urn = :ptiUrn and dc.visible = true and c.isCivil = :isCivil",
                        DefenceClient.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("dateOfBirth", dateOfBirth)
                .setParameter("ptiUrn", ptiUrn)
                .setParameter("isCivil", isCivil)
                .getResultList();
    }

    public List<DefenceClient> findDefenceClientByCriteriaWithOutDob(final String firstName, final String lastName,
            final String ptiUrn, final boolean isCivil) {
        return entityManager.createQuery(
                        "select dc FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE upper(dc.firstName) = upper(:firstName) and upper(dc.lastName) = upper(:lastName) and c.urn = :ptiUrn and dc.visible = true and c.isCivil = :isCivil",
                        DefenceClient.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("ptiUrn", ptiUrn)
                .setParameter("isCivil", isCivil)
                .getResultList();
    }

    public List<DefenceClient> findDefenceClientByCriteria(final String firstName, final String lastName,
            final LocalDate dateOfBirth) {
        return entityManager.createQuery(
                        "select dc FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and dc.dateOfBirth = :dateOfBirth and dc.visible = true",
                        DefenceClient.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("dateOfBirth", dateOfBirth)
                .getResultList();
    }

    public List<DefenceClient> findDefenceClientByCriteria(final String firstName, final String lastName,
            final LocalDate dateOfBirth, final boolean isCivil) {
        return entityManager.createQuery(
                        "select dc FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and dc.dateOfBirth = :dateOfBirth and dc.visible = true and c.isCivil = :isCivil",
                        DefenceClient.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("dateOfBirth", dateOfBirth)
                .setParameter("isCivil", isCivil)
                .getResultList();
    }

    public List<DefenceClient> findDefenceClientByCriteriaWithOutDob(final String firstName, final String lastName,
            final boolean isCivil) {
        return entityManager.createQuery(
                        "select dc FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE upper(dc.firstName) = upper(:firstName) and upper(dc.lastName) = upper(:lastName) and dc.visible = true and c.isCivil = :isCivil",
                        DefenceClient.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("isCivil", isCivil)
                .getResultList();
    }

    public List<DefenceClient> findDefenceClientByCriteria(final String organisationName, final String ptiUrn) {
        return entityManager.createQuery(
                        "select dc FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName) and upper(c.urn) = upper(:ptiUrn) and dc.visible = true and dc.caseId = c.id",
                        DefenceClient.class)
                .setParameter("organisationName", organisationName)
                .setParameter("ptiUrn", ptiUrn)
                .getResultList();
    }

    public List<DefenceClient> findDefenceClientByCriteria(final String organisationName, final String ptiUrn,
            final boolean isCivil) {
        return entityManager.createQuery(
                        "select dc FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName) and upper(c.urn) = upper(:ptiUrn) and dc.visible = true and dc.caseId = c.id and c.isCivil = :isCivil",
                        DefenceClient.class)
                .setParameter("organisationName", organisationName)
                .setParameter("ptiUrn", ptiUrn)
                .setParameter("isCivil", isCivil)
                .getResultList();
    }

    public List<DefenceClient> findDefenceClientByCriteria(final String organisationName) {
        return entityManager.createQuery(
                        "select dc FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName) and dc.visible = true and dc.caseId = c.id",
                        DefenceClient.class)
                .setParameter("organisationName", organisationName)
                .getResultList();
    }

    public List<DefenceClient> findDefenceClientByCriteria(final String organisationName, final boolean isCivil) {
        return entityManager.createQuery(
                        "select dc FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName) and dc.visible = true and dc.caseId = c.id and c.isCivil = :isCivil",
                        DefenceClient.class)
                .setParameter("organisationName", organisationName)
                .setParameter("isCivil", isCivil)
                .getResultList();
    }

    public DefenceClient findOptionalByDefendantId(final UUID defendantId) {
        return entityManager.createQuery(
                        "SELECT dc FROM DefenceClient dc WHERE dc.defendantId = :defendantId", DefenceClient.class)
                .setParameter("defendantId", defendantId)
                .getResultStream().findFirst().orElse(null);
    }

    public DefenceClient findDefenceClientByCriteria(final UUID defendantId) {
        return entityManager.createQuery(
                        "select dc FROM DefenceClient dc WHERE dc.defendantId = :defendantId", DefenceClient.class)
                .setParameter("defendantId", defendantId)
                .getResultStream().findFirst().orElse(null);
    }

    public DefenceClient findOptionalByDefendantIdAndCaseId(final UUID defendantId, final UUID caseId) {
        return entityManager.createQuery(
                        "SELECT dc FROM DefenceClient dc WHERE dc.defendantId = :defendantId AND dc.caseId = :caseId",
                        DefenceClient.class)
                .setParameter("defendantId", defendantId)
                .setParameter("caseId", caseId)
                .getResultStream().findFirst().orElse(null);
    }

    public List<DefenceClient> findByCaseId(final UUID caseId) {
        return entityManager.createQuery(
                        "SELECT dc FROM DefenceClient dc WHERE dc.caseId = :caseId", DefenceClient.class)
                .setParameter("caseId", caseId)
                .getResultList();
    }

    public List<UUID> findCasesAssociatedWithDefenceClientByPersonDefendant(final String firstName,
            final String lastName, final LocalDate dateOfBirth) {
        return entityManager.createQuery(
                        "SELECT dc.caseId FROM DefenceClient dc, DefenceCase c WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and dc.dateOfBirth = :dateOfBirth and dc.visible = true and dc.caseId = c.id",
                        UUID.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("dateOfBirth", dateOfBirth)
                .getResultList();
    }

    public List<UUID> findCasesAssociatedWithDefenceClientByPersonDefendant(final String firstName,
            final String lastName, final LocalDate dateOfBirth, final boolean isCivil, final boolean isGroupMember) {
        return entityManager.createQuery(
                        "SELECT dc.caseId FROM DefenceClient dc, DefenceCase c WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and dc.dateOfBirth = :dateOfBirth and dc.visible = true and dc.caseId = c.id and c.isCivil = :isCivil and c.isGroupMember = :isGroupMember",
                        UUID.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("dateOfBirth", dateOfBirth)
                .setParameter("isCivil", isCivil)
                .setParameter("isGroupMember", isGroupMember)
                .getResultList();
    }

    public List<UUID> findCasesAssociatedWithDefenceClientByPersonDefendantWithoutDob(final String firstName,
            final String lastName, final boolean isCivil, final boolean isGroupMember) {
        return entityManager.createQuery(
                        "SELECT DISTINCT dc.caseId FROM DefenceClient dc, DefenceCase c WHERE upper(dc.firstName) = upper(:firstName) and upper(dc.lastName) = upper(:lastName) and dc.visible = true and dc.caseId = c.id and c.isCivil = :isCivil and c.isGroupMember = :isGroupMember",
                        UUID.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("isCivil", isCivil)
                .setParameter("isGroupMember", isGroupMember)
                .getResultList();
    }

    public List<UUID> findCasesAssociatedWithDefenceClientByOrganisationDefendant(final String organisationName) {
        return entityManager.createQuery(
                        "SELECT dc.caseId FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName) and dc.visible = true and dc.caseId = c.id",
                        UUID.class)
                .setParameter("organisationName", organisationName)
                .getResultList();
    }

    public List<UUID> findCasesAssociatedWithDefenceClientByOrganisationDefendant(final String organisationName,
            final boolean isCivil, final boolean isGroupMember) {
        return entityManager.createQuery(
                        "SELECT dc.caseId FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName) and dc.visible = true and dc.caseId = c.id and c.isCivil = :isCivil and c.isGroupMember = :isGroupMember",
                        UUID.class)
                .setParameter("organisationName", organisationName)
                .setParameter("isCivil", isCivil)
                .setParameter("isGroupMember", isGroupMember)
                .getResultList();
    }

    public List<UUID> getPersonDefendant(final String firstName, final String lastName, final LocalDate dateOfBirth) {
        return entityManager.createQuery(
                        "SELECT dc.defendantId FROM DefenceClient dc WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and dc.dateOfBirth = :dateOfBirth",
                        UUID.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("dateOfBirth", dateOfBirth)
                .getResultList();
    }

    public List<UUID> getPersonDefendant(final String firstName, final String lastName, final LocalDate dateOfBirth,
            final boolean isCivil, final boolean isGroupMember) {
        return entityManager.createQuery(
                        "SELECT dc.defendantId FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and dc.dateOfBirth = :dateOfBirth and c.isCivil = :isCivil and c.isGroupMember = :isGroupMember",
                        UUID.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("dateOfBirth", dateOfBirth)
                .setParameter("isCivil", isCivil)
                .setParameter("isGroupMember", isGroupMember)
                .getResultList();
    }

    public List<UUID> getPersonDefendantWithOutDob(final String firstName, final String lastName,
            final boolean isCivil, final boolean isGroupMember) {
        return entityManager.createQuery(
                        "SELECT dc.defendantId FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE upper(dc.firstName) = upper(:firstName) and upper(dc.lastName) = upper(:lastName) and c.isCivil = :isCivil and c.isGroupMember = :isGroupMember",
                        UUID.class)
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("isCivil", isCivil)
                .setParameter("isGroupMember", isGroupMember)
                .getResultList();
    }

    public List<UUID> getOrganisationDefendant(final String organisationName) {
        return entityManager.createQuery(
                        "SELECT dc.defendantId FROM DefenceClient dc WHERE upper(dc.organisationName) = upper(:organisationName)",
                        UUID.class)
                .setParameter("organisationName", organisationName)
                .getResultList();
    }

    public List<UUID> getOrganisationDefendant(final String organisationName, final boolean isCivil,
            final boolean isGroupMember) {
        return entityManager.createQuery(
                        "SELECT dc.defendantId FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE upper(dc.organisationName) = upper(:organisationName) and c.isCivil = :isCivil and c.isGroupMember = :isGroupMember",
                        UUID.class)
                .setParameter("organisationName", organisationName)
                .setParameter("isCivil", isCivil)
                .setParameter("isGroupMember", isGroupMember)
                .getResultList();
    }

    public DefenceClient save(final DefenceClient entity) {
        return entityManager.merge(entity);
    }

    public void refresh(final DefenceClient entity) {
        final DefenceClient managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.refresh(managed);
    }

    public void remove(final DefenceClient entity) {
        final DefenceClient managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }
}
