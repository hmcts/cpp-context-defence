package uk.gov.moj.cpp.defence.persistence;


import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionOrganisationAccess;
import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionOrganisationCaseKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.MaxResults;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface OrganisationAccessRepository extends EntityRepository<ProsecutionOrganisationAccess, ProsecutionOrganisationCaseKey> {

    @Query("from ProsecutionOrganisationAccess poa where poa.id.caseId=:caseId")
    List<ProsecutionOrganisationAccess> findByCaseId(@QueryParam("caseId") UUID caseId);

    @Query("from ProsecutionOrganisationAccess poa where poa.id.assigneeOrganisationId=:assigneeOrganisationId and poa.id.caseId=:caseId")
    Optional<ProsecutionOrganisationAccess> findByAssigneeOrganisationIdAndCaseId(@QueryParam("assigneeOrganisationId") UUID assigneeOrganisationId, @QueryParam("caseId") UUID caseId);

    @Query("from ProsecutionOrganisationAccess poa where poa.id.caseId=:caseId and poa.id.assigneeOrganisationId=:assigneeOrganisationId")
    List<ProsecutionOrganisationAccess> findByCaseIdAndAssigneeOrganisationId(@QueryParam("caseId") UUID caseId, @QueryParam("assigneeOrganisationId") UUID assigneeOrganisationId);

    @Query("from ProsecutionOrganisationAccess poa where poa.id.caseId=:caseId and poa.id.assigneeOrganisationId=:assigneeOrganisationId and (poa.assignmentExpiryDate is null or poa.assignmentExpiryDate > now())")
    List<ProsecutionOrganisationAccess> findActiveByCaseIdAndAssigneeOrganisationId(@QueryParam("caseId") UUID caseId, @QueryParam("assigneeOrganisationId") UUID assigneeOrganisationId);

    @Query(value = "from ProsecutionOrganisationAccess poa where poa.assignmentExpiryDate  < now() and poa.prosecutionAdvocatesWithAccess is EMPTY order by poa.assignmentExpiryDate desc")
    List<ProsecutionOrganisationAccess> findExpiredCaseAssignments();

    @Query(value = "from ProsecutionOrganisationAccess poa where poa.assignmentExpiryDate  < now() and poa.prosecutionAdvocatesWithAccess is EMPTY order by poa.assignmentExpiryDate desc")
    List<ProsecutionOrganisationAccess> findExpiredCaseAssignments(@MaxResults int max);
}