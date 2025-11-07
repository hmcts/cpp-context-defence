package uk.gov.moj.cpp.defence.persistence;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalJunit4Test;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceCase;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class DefenceCaseRepositoryIT extends BaseTransactionalJunit4Test {

    @Inject
    private DefenceCaseRepository defenceCaseRepository;

    @Test
    public void findIdpcDetailsForDefenceClientId() {

        UUID caseId = randomUUID();
        String urn = "Test URN";

        DefenceCase defenceCase = new DefenceCase();
        defenceCase.setId(caseId);
        defenceCase.setUrn(urn);

        defenceCaseRepository.save(defenceCase);

        DefenceCase savedCase = defenceCaseRepository.findBy(caseId);
        assertEquals(defenceCase, savedCase);

        savedCase = defenceCaseRepository.findOptionalByUrn(urn);
        assertEquals(defenceCase, savedCase);
    }

    @Test
    public void shouldReturnNullWhenDefenceClientNotKnown() {
        DefenceCase random = defenceCaseRepository.findBy(UUID.randomUUID());
        assertNull(random);

        random = defenceCaseRepository.findOptionalByUrn("random");
        assertNull(random);

    }
}
