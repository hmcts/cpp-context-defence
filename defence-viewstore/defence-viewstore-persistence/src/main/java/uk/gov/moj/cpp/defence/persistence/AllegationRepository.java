package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.Allegation;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface AllegationRepository extends EntityRepository<Allegation, UUID> {

    @Query(value = "FROM Allegation alg WHERE alg.defenceClient = :defenceClientId AND alg.defenceClient IN (SELECT dc.id FROM DefenceClient dc WHERE dc.id = :defenceClientId AND dc.visible = true)")
    List<Allegation> findAllegationByCriteria(@QueryParam("defenceClientId") final UUID defenceClientId);

    @Query(value = "FROM Allegation alg WHERE alg.defenceClientId = :defenceClientId AND alg.offenceId = :offenceId")
    Allegation findAllegationByDefenceClientIdAndOffenceId(@QueryParam("defenceClientId") final UUID defenceClientId, @QueryParam("offenceId") final UUID offenceId);

}
