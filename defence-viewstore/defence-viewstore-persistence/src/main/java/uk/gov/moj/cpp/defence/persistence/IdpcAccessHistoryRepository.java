package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.IdpcAccess;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface IdpcAccessHistoryRepository extends EntityRepository<IdpcAccess, UUID> {

    @Query(value = "FROM IdpcAccess ia WHERE ia.defenceClientId = :defenceClientId")
    List<IdpcAccess> findIdpcAccessByCriteria(@QueryParam("defenceClientId") final UUID defenceClientId);

    @Query(value = "FROM IdpcAccess ia WHERE ia.defenceClientId = :defenceClientId and ia.idpcId = :idpcId")
    List<IdpcAccess> findIdpcAccessByCriteria(@QueryParam("defenceClientId") final UUID defenceClientId,
                                              @QueryParam("idpcId") final UUID idpcId);

    @Query(value = "Select IdpcAccess.organisationId FROM IdpcAccess ia WHERE ia.defenceClientId = :defenceClientId and ia.idpcId = :idpcId")
    List<UUID> findIdpcAccessOrganisationByCriteria(@QueryParam("defenceClientId") final UUID defenceClientId,
                                              @QueryParam("idpcId") final UUID idpcId);

    @Query(value = "Select  ia.organisationId FROM IdpcAccess ia  WHERE ia.defenceClientId = :defenceClientId GROUP BY ia.organisationId ORDER BY max(accessTimestamp) DESC")
    List<UUID> findOrderedDistinctOrgIdsOfIdpcAccessForDefenceClient(@QueryParam("defenceClientId") final UUID defenceClientId);
}
