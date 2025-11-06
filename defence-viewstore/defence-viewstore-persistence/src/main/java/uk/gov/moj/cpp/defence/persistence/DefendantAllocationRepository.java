package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefendantAllocation;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface DefendantAllocationRepository extends EntityRepository<DefendantAllocation, UUID> {


    @Query(value = " from DefendantAllocation p where p.defendantId in (select dc.defendantId from DefenceClient dc where dc.caseId =:caseId)")
    List<DefendantAllocation> findDefendantAllocationByCaseId(@QueryParam("caseId") final UUID caseId);


}