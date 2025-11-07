package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.Instruction;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface InstructionRepository extends EntityRepository<Instruction, UUID> {

    @Query(value = "FROM Instruction ins WHERE ins.defenceClient.id = :defenceClientId and ins.userId = :userId")
    List<Instruction> findInstructionsByCriteria(@QueryParam("defenceClientId") final UUID defenceClientId, @QueryParam("userId") final UUID userId);

    @Query(value = "Select Count(ins) FROM Instruction ins WHERE ins.defenceClient.id = :defenceClientId and ins.userId = :userId")
    int findNumberOfInstructionsForUserForDefenceClient(@QueryParam("defenceClientId") final UUID defenceClientId, @QueryParam("userId") final UUID userId);

    @Query(value = "Select Count(ins) FROM Instruction ins WHERE ins.defenceClient.id = :defenceClientId and ins.organisationId = :organisationId")
    int findNumberOfInstructionsByCriteria(@QueryParam("defenceClientId") final UUID defenceClientId, @QueryParam("organisationId") final UUID organisationId);

}
