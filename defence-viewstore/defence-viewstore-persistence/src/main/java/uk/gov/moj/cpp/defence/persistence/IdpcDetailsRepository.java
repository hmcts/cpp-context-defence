package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.IdpcDetails;

import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.SingleResultType;

@Repository
public interface IdpcDetailsRepository extends EntityRepository<IdpcDetails, UUID> {

    @Query(value = "FROM IdpcDetails idpc WHERE idpc.defenceClientId = :defenceClientId")
    IdpcDetails findIdpcDetailsForDefenceClient(@QueryParam("defenceClientId") final UUID defenceClientId);

    IdpcDetails findOptionalByDefenceClientId(final UUID defenceClientId);

    @Query(value = "select idpc FROM IdpcDetails idpc where idpc.defenceClientId in ( select dc.id from DefenceClient dc where defendantId=:defendantId) ",singleResult = SingleResultType.OPTIONAL)
    IdpcDetails findIdpcDetailsForDefendantId(@QueryParam("defendantId")final UUID defendantId);

}
