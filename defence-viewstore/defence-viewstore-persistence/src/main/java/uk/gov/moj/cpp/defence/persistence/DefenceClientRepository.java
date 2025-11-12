package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface DefenceClientRepository extends EntityRepository<DefenceClient, UUID> {

    @Query(value = "select dc FROM DefenceClient dc INNER JOIN  DefenceCase c ON c.id = dc.caseId WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and "
            + "dc.dateOfBirth = :dateOfBirth  and c.urn = :ptiUrn and dc.visible = true")
    List<DefenceClient> findDefenceClientByCriteria(@QueryParam("firstName") final String firstName,
                                                    @QueryParam("lastName") final String lastName,
                                                    @QueryParam("dateOfBirth") final LocalDate dateOfBirth,
                                                    @QueryParam("ptiUrn") final String ptiUrn);

    @Query(value = "select dc FROM DefenceClient dc INNER JOIN  DefenceCase c ON c.id = dc.caseId WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and "
            + "dc.dateOfBirth = :dateOfBirth  and c.urn = :ptiUrn and dc.visible = true and c.isCivil = :isCivil")
    List<DefenceClient> findDefenceClientByCriteria(@QueryParam("firstName") final String firstName,
                                                    @QueryParam("lastName") final String lastName,
                                                    @QueryParam("dateOfBirth") final LocalDate dateOfBirth,
                                                    @QueryParam("ptiUrn") final String ptiUrn,
                                                    @QueryParam("isCivil") final boolean isCivil);

    @Query(value = "select dc FROM DefenceClient dc INNER JOIN  DefenceCase c ON c.id = dc.caseId WHERE upper(dc.firstName) = upper(:firstName) and upper(dc.lastName) = upper(:lastName) and "
            + "c.urn = :ptiUrn and dc.visible = true and c.isCivil = :isCivil")
    List<DefenceClient> findDefenceClientByCriteriaWithOutDob(@QueryParam("firstName") final String firstName,
                                                    @QueryParam("lastName") final String lastName,
                                                    @QueryParam("ptiUrn") final String ptiUrn,
                                                    @QueryParam("isCivil") final boolean isCivil);

    @Query(value = "select dc FROM DefenceClient dc INNER JOIN  DefenceCase c ON  c.id = dc.caseId WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and "
            + "dc.dateOfBirth = :dateOfBirth and dc.visible = true")
    List<DefenceClient> findDefenceClientByCriteria(@QueryParam("firstName") final String firstName,
                                                    @QueryParam("lastName") final String lastName,
                                                    @QueryParam("dateOfBirth") final LocalDate dateOfBirth);

    @Query(value = "select dc FROM DefenceClient dc INNER JOIN  DefenceCase c ON  c.id = dc.caseId WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and "
            + "dc.dateOfBirth = :dateOfBirth and dc.visible = true and c.isCivil = :isCivil")
    List<DefenceClient> findDefenceClientByCriteria(@QueryParam("firstName") final String firstName,
                                                    @QueryParam("lastName") final String lastName,
                                                    @QueryParam("dateOfBirth") final LocalDate dateOfBirth,
                                                    @QueryParam("isCivil") final boolean isCivil);

    @Query(value = "select dc FROM DefenceClient dc INNER JOIN  DefenceCase c ON  c.id = dc.caseId WHERE upper(dc.firstName) = upper(:firstName) and upper(dc.lastName) = upper(:lastName) and "
            + "dc.visible = true and c.isCivil = :isCivil")
    List<DefenceClient> findDefenceClientByCriteriaWithOutDob(@QueryParam("firstName") final String firstName,
                                                    @QueryParam("lastName") final String lastName,
                                                    @QueryParam("isCivil") final boolean isCivil);

    @Query(value = "select dc FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName)  and upper(c.urn) = upper(:ptiUrn) and"
            + " dc.visible = true and dc.caseId = c.id")
    List<DefenceClient> findDefenceClientByCriteria(@QueryParam("organisationName") final String organisationName,
                                                    @QueryParam("ptiUrn") final String ptiUrn);

    @Query(value = "select dc FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName)  and upper(c.urn) = upper(:ptiUrn) and"
            + " dc.visible = true and dc.caseId = c.id and c.isCivil = :isCivil")
    List<DefenceClient> findDefenceClientByCriteria(@QueryParam("organisationName") final String organisationName,
                                                    @QueryParam("ptiUrn") final String ptiUrn,
                                                    @QueryParam("isCivil") final boolean isCivil);

    @Query(value = "select dc FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName) and"
            + " dc.visible = true and dc.caseId = c.id")
    List<DefenceClient> findDefenceClientByCriteria(@QueryParam("organisationName") final String organisationName);

    @Query(value = "select dc FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName) and"
            + " dc.visible = true and dc.caseId = c.id and c.isCivil = :isCivil")
    List<DefenceClient> findDefenceClientByCriteria(@QueryParam("organisationName") final String organisationName,
                                                    @QueryParam("isCivil") final boolean isCivil);

    DefenceClient findOptionalByDefendantId(UUID defendantId);

    @Query(value = "select dc FROM DefenceClient dc WHERE dc.defendantId = :defendantId")
    DefenceClient findDefenceClientByCriteria(@QueryParam("defendantId") final UUID defendantId);

    DefenceClient findOptionalByDefendantIdAndCaseId(UUID defendantId, UUID caseId);

    List<DefenceClient> findByCaseId(UUID caseId);


    @Query(value = "SELECT dc.caseId FROM DefenceClient dc, DefenceCase c WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and "
            + "dc.dateOfBirth = :dateOfBirth and dc.visible = true and dc.caseId = c.id")
    List<UUID> findCasesAssociatedWithDefenceClientByPersonDefendant(@QueryParam("firstName") final String firstName,
                                                                     @QueryParam("lastName") final String lastName,
                                                                     @QueryParam("dateOfBirth") final LocalDate dateOfBirth);

    @Query(value = "SELECT dc.caseId FROM DefenceClient dc, DefenceCase c WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and "
            + "dc.dateOfBirth = :dateOfBirth and dc.visible = true and dc.caseId = c.id and c.isCivil = :isCivil and c.isGroupMember = :isGroupMember")
    List<UUID> findCasesAssociatedWithDefenceClientByPersonDefendant(@QueryParam("firstName") final String firstName,
                                                                     @QueryParam("lastName") final String lastName,
                                                                     @QueryParam("dateOfBirth") final LocalDate dateOfBirth,
                                                                     @QueryParam("isCivil") final boolean isCivil,
                                                                     @QueryParam("isGroupMember") final boolean isGroupMember);

    @Query(value = "SELECT DISTINCT dc.caseId FROM DefenceClient dc, DefenceCase c WHERE upper(dc.firstName) = upper(:firstName) and upper(dc.lastName) = upper(:lastName) and "
            + "dc.visible = true and dc.caseId = c.id and c.isCivil = :isCivil and c.isGroupMember = :isGroupMember")
    List<UUID> findCasesAssociatedWithDefenceClientByPersonDefendantWithoutDob(@QueryParam("firstName") final String firstName,
                                                                     @QueryParam("lastName") final String lastName,
                                                                     @QueryParam("isCivil") final boolean isCivil,
                                                                     @QueryParam("isGroupMember") final boolean isGroupMember);


    @Query(value = "SELECT dc.caseId FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName) and"
            + " dc.visible = true and dc.caseId = c.id")
    List<UUID> findCasesAssociatedWithDefenceClientByOrganisationDefendant(@QueryParam("organisationName") final String organisationName);

    @Query(value = "SELECT dc.caseId FROM DefenceClient dc, DefenceCase c WHERE upper(dc.organisationName) = upper(:organisationName) and"
            + " dc.visible = true and dc.caseId = c.id and c.isCivil = :isCivil and c.isGroupMember = :isGroupMember")
    List<UUID> findCasesAssociatedWithDefenceClientByOrganisationDefendant(@QueryParam("organisationName") final String organisationName,
                                                                           @QueryParam("isCivil") final boolean isCivil,
                                                                           @QueryParam("isGroupMember") final boolean isGroupMember);


    @Query(value = "SELECT dc.defendantId FROM DefenceClient dc WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and "
            + "dc.dateOfBirth = :dateOfBirth ")
    List<UUID> getPersonDefendant(@QueryParam("firstName") final String firstName,
                                  @QueryParam("lastName") final String lastName,
                                  @QueryParam("dateOfBirth") final LocalDate dateOfBirth);

    @Query(value = "SELECT dc.defendantId FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE TRIM(upper(dc.firstName)) = upper(:firstName) and TRIM(upper(dc.lastName)) = upper(:lastName) and "
            + "dc.dateOfBirth = :dateOfBirth and c.isCivil = :isCivil and c.isGroupMember = :isGroupMember")
    List<UUID> getPersonDefendant(@QueryParam("firstName") final String firstName,
                                  @QueryParam("lastName") final String lastName,
                                  @QueryParam("dateOfBirth") final LocalDate dateOfBirth,
                                  @QueryParam("isCivil") final boolean isCivil,
                                  @QueryParam("isGroupMember") final boolean isGroupMember);

    @Query(value = "SELECT dc.defendantId FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE upper(dc.firstName) = upper(:firstName) and upper(dc.lastName) = upper(:lastName) and "
            + "c.isCivil = :isCivil and c.isGroupMember = :isGroupMember")
    List<UUID> getPersonDefendantWithOutDob(@QueryParam("firstName") final String firstName,
                                            @QueryParam("lastName") final String lastName,
                                            @QueryParam("isCivil") final boolean isCivil,
                                            @QueryParam("isGroupMember") final boolean isGroupMember);


    @Query(value = "SELECT dc.defendantId FROM DefenceClient dc WHERE upper(dc.organisationName) = upper(:organisationName)")
    List<UUID> getOrganisationDefendant(@QueryParam("organisationName") final String organisationName);

    @Query(value = "SELECT dc.defendantId FROM DefenceClient dc INNER JOIN DefenceCase c ON c.id = dc.caseId WHERE upper(dc.organisationName) = upper(:organisationName) and c.isCivil = :isCivil" +
            " and c.isGroupMember = :isGroupMember")
    List<UUID> getOrganisationDefendant(@QueryParam("organisationName") final String organisationName,
                                        @QueryParam("isCivil") final boolean isCivil,
                                        @QueryParam("isGroupMember") final boolean isGroupMember);

}
