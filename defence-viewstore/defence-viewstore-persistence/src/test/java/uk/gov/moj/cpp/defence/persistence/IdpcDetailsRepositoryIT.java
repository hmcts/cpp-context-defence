package uk.gov.moj.cpp.defence.persistence;

import static java.time.LocalDate.now;
import static java.time.LocalDate.of;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalJunit4Test;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;
import uk.gov.moj.cpp.defence.persistence.entity.IdpcDetails;

import java.time.LocalDate;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class IdpcDetailsRepositoryIT extends BaseTransactionalJunit4Test {

    @Inject
    IdpcDetailsRepository idpcRepository;

    @Inject
    DefenceClientRepository defenceClientRepository;

    @Test
    public void findIdpcDetailsForDefenceClientId() {

        uk.gov.moj.cpp.defence.IdpcDetails idpcDetailsVo = uk.gov.moj.cpp.defence.IdpcDetails.idpcDetails()
                .withPublishedDate(now())
                .withSize("2.7Mb")
                .withPageCount(20)
                .withMaterialId(randomUUID())
                .build();

        final IdpcDetails expectedIdpcDetails = new IdpcDetails(randomUUID(), randomUUID(), idpcDetailsVo, "SURNAME firstname 11DD0304617 Initial Details Pros Case");
        expectedIdpcDetails.setPageCount(20);
        expectedIdpcDetails.setPublishedDate(now());

        idpcRepository.save(expectedIdpcDetails);

        final IdpcDetails actualIdpcDetails = idpcRepository.findIdpcDetailsForDefenceClient(expectedIdpcDetails.getDefenceClientId());

        assertEquals(expectedIdpcDetails, actualIdpcDetails);

        final IdpcDetails idpcDetailsById = idpcRepository.findBy(expectedIdpcDetails.getId());

        assertEquals(expectedIdpcDetails, idpcDetailsById);
    }

    @Test
    public void shouldReturnNullWhenDefenceClientNotKnown() {
        final IdpcDetails actualIdpcDetails = idpcRepository.findOptionalByDefenceClientId(UUID.randomUUID());
        assertEquals(null, actualIdpcDetails);
    }

    @Test
    public void findIdpcDetailsForDefendantId() {
        DefenceClient defenceClient = getDefenceClient1(randomUUID());
        defenceClientRepository.save(defenceClient);

        uk.gov.moj.cpp.defence.IdpcDetails idpcDetailsVo = uk.gov.moj.cpp.defence.IdpcDetails.idpcDetails()
                .withPublishedDate(now())
                .withSize("2.7Mb")
                .withPageCount(20)
                .withMaterialId(randomUUID())
                .build();

        final IdpcDetails expectedIdpcDetails = new IdpcDetails(randomUUID(), defenceClient.getId(), idpcDetailsVo, "SURNAME firstname 11DD0304617 Initial Details Pros Case");
        expectedIdpcDetails.setPageCount(20);
        expectedIdpcDetails.setPublishedDate(now());

        idpcRepository.save(expectedIdpcDetails);

        final IdpcDetails actualIdpcDetails = idpcRepository.findIdpcDetailsForDefendantId(defenceClient.getDefendantId());

        assertEquals(expectedIdpcDetails, actualIdpcDetails);

        final IdpcDetails idpcDetailsById = idpcRepository.findBy(expectedIdpcDetails.getId());

        assertEquals(expectedIdpcDetails, idpcDetailsById);
    }

    @Test
    public void findIdpcDetailsForDefendantIdNotFound() {
        DefenceClient defenceClient = getDefenceClient1(randomUUID());
        defenceClientRepository.save(defenceClient);

        defenceClient.setDefendantId(null);
        IdpcDetails actualIdpcDetails = null;
        actualIdpcDetails = idpcRepository.findIdpcDetailsForDefendantId(defenceClient.getDefendantId());
        assertNull(actualIdpcDetails);
    }

    private DefenceClient getDefenceClient1(final UUID caseId) {
        final String defenceClientOneFirstName = "TEST ONE FIRST NAME";
        final String defenceClientOneLastName = "TEST ONE LAST NAME";
        final LocalDate defenceClientOneDob = of(1985, 10, 21);

        return new DefenceClient(randomUUID(), defenceClientOneFirstName, defenceClientOneLastName, caseId, defenceClientOneDob, randomUUID());
    }
}