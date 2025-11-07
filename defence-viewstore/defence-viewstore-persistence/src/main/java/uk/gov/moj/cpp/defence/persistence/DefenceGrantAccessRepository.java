package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefenceGrantAccess;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface DefenceGrantAccessRepository extends EntityRepository<DefenceGrantAccess, UUID> {

    @Query(value = "select dga FROM DefenceGrantAccess dga WHERE dga.defenceClient.id = :defendantClientId and dga.removed = false")
    List<DefenceGrantAccess> findByDefenceClient(@QueryParam("defendantClientId") final UUID defendantClientId);

    @Query(value = "select dga FROM DefenceGrantAccess dga WHERE dga.defenceClient.id = :defendantClientId and dga.granteeDefenceUserDetails.userId  = :userId and dga.removed = false")
    DefenceGrantAccess findByDefenceClient(@QueryParam("defendantClientId") final UUID defendantClientId, @QueryParam("userId") final UUID userId);

    @Query(value = "select dga FROM DefenceGrantAccess dga WHERE dga.defenceClient.caseId = :caseId and dga.granteeDefenceUserDetails.userId  = :userId and dga.removed = false")
    List<DefenceGrantAccess> findByGranteeAndCaseId(@QueryParam("caseId") final UUID caseId, @QueryParam("userId") final UUID userId);
}
