package uk.gov.moj.cpp.defence.persistence;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import uk.gov.moj.cpp.defence.persistence.entity.ProsecutionAdvocateAccess;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository(forEntity = ProsecutionAdvocateAccess.class )
public abstract class AdvocateAccessRepository extends AbstractEntityRepository<ProsecutionAdvocateAccess, UUID> {

    @Query(value = "select entity from ProsecutionAdvocateAccess entity where entity.prosecutionOrganisation.id.caseId = :caseId and entity.assigneeDetails.userId = :assigneeId")
    public abstract List<ProsecutionAdvocateAccess> findByCaseIdAndAssigneeId(@QueryParam("caseId") final UUID caseId, @QueryParam("assigneeId") final UUID assigneeId);

    @Query(value = "select entity from ProsecutionAdvocateAccess entity where entity.prosecutionOrganisation.id.caseId = :caseId and entity.assigneeDetails.userId = :assigneeId and (entity.assignmentExpiryDate is null or entity.assignmentExpiryDate > now())")
    public abstract List<ProsecutionAdvocateAccess> findActiveByCaseIdAndAssigneeId(@QueryParam("caseId") final UUID caseId, @QueryParam("assigneeId") final UUID assigneeId);

    @Query(value = "select entity from ProsecutionAdvocateAccess entity where entity.assignmentExpiryDate  < now() order by entity.assignmentExpiryDate desc")
    public abstract List<ProsecutionAdvocateAccess> findExpiredCaseAssignments();

    public List<ProsecutionAdvocateAccess> findExpiredCaseAssignments(final int limitCount){

        return entityManager().createNativeQuery("select pa.*  from prosecution_advocate_access pa  JOIN prosecution_organisation_access po ON pa.assignee_organisation_id = po.assignee_organisation_id AND pa.case_id = po.case_id where pa.assignment_expiry_date  < now() order by pa.assignment_expiry_date desc",
                ProsecutionAdvocateAccess.class).setMaxResults(limitCount).getResultList();
    }


}
