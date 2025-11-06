package uk.gov.moj.cpp.defence.persistence;


import uk.gov.moj.cpp.defence.persistence.entity.DefenceAssociationDefendant;

import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface DefenceAssociationDefendantRepository extends EntityRepository<DefenceAssociationDefendant, UUID> {

    DefenceAssociationDefendant findOptionalByDefendantId(UUID defendantId);

}
