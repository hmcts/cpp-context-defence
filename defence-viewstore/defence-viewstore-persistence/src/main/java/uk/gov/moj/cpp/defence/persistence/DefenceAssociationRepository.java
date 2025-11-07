package uk.gov.moj.cpp.defence.persistence;


import uk.gov.moj.cpp.defence.persistence.entity.DefenceAssociation;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface DefenceAssociationRepository extends EntityRepository<DefenceAssociation, UUID> {

    @Query(value = "from DefenceAssociation entity where entity.laaContractNumber in (:laaContractNumbers) and end_date is null")
    List<DefenceAssociation> findByLAAContractNumber(@QueryParam("laaContractNumbers") Collection<String> laaContractNumbers);

    @Query(value = "select da from DefenceAssociation da, DefenceClient dc where da.orgId = :organisationId and da.defenceAssociationDefendant.defendantId=dc.defendantId and dc.caseId = :caseId")
    List<DefenceAssociation> findByOrganisationIdAndCaseId(
            @QueryParam("organisationId") final UUID organisationId,
            @QueryParam("caseId") final UUID caseId);

    @Query(value = "FROM DefenceAssociation da WHERE da.userId = :userId AND (da.endDate is null OR da.startDate <= :currentDate AND da.endDate >= :currentDate)")
    List<DefenceAssociation> findByUserIdAndCurrentDate(@QueryParam("userId") final UUID userId, @QueryParam("currentDate") final ZonedDateTime currentDate);

}
