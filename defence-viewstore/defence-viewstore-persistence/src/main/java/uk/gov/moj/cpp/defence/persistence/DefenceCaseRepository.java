package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefenceCase;

import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface DefenceCaseRepository extends EntityRepository<DefenceCase, UUID> {

    DefenceCase findOptionalByUrn(String urn);

}
