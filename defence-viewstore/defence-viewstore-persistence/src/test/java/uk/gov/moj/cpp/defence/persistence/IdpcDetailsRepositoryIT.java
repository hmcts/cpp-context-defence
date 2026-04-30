package uk.gov.moj.cpp.defence.persistence;

import static java.time.LocalDate.now;
import static java.time.LocalDate.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;
import uk.gov.moj.cpp.defence.persistence.entity.IdpcDetails;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class IdpcDetailsRepositoryIT {

    private static final String PERSISTENCE_UNIT = "defence-test-persistence-unit";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private IdpcDetailsRepository idpcRepository;
    private DefenceClientRepository defenceClientRepository;

    @BeforeEach
    public void setUpRepositories() {
        idpcRepository = new IdpcDetailsRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(idpcRepository);
        defenceClientRepository = new DefenceClientRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(defenceClientRepository);
    }

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

        assertIdpcDetailsEqual(actualIdpcDetails, expectedIdpcDetails);

        final IdpcDetails idpcDetailsById = idpcRepository.findBy(expectedIdpcDetails.getId());

        assertIdpcDetailsEqual(idpcDetailsById, expectedIdpcDetails);
    }

    @Test
    public void shouldReturnNullWhenDefenceClientNotKnown() {
        final IdpcDetails actualIdpcDetails = idpcRepository.findOptionalByDefenceClientId(UUID.randomUUID());
        assertThat(actualIdpcDetails, is(nullValue()));
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

        assertIdpcDetailsEqual(actualIdpcDetails, expectedIdpcDetails);

        final IdpcDetails idpcDetailsById = idpcRepository.findBy(expectedIdpcDetails.getId());

        assertIdpcDetailsEqual(idpcDetailsById, expectedIdpcDetails);
    }

    @Test
    public void findIdpcDetailsForDefendantIdNotFound() {
        DefenceClient defenceClient = getDefenceClient1(randomUUID());
        defenceClientRepository.save(defenceClient);

        defenceClient.setDefendantId(null);
        IdpcDetails actualIdpcDetails = idpcRepository.findIdpcDetailsForDefendantId(defenceClient.getDefendantId());
        assertThat(actualIdpcDetails, is(nullValue()));
    }

    private void assertIdpcDetailsEqual(final IdpcDetails actual, final IdpcDetails expected) {
        assertThat(actual.getId(), is(expected.getId()));
        assertThat(actual.getDefenceClientId(), is(expected.getDefenceClientId()));
        assertThat(actual.getMaterialId(), is(expected.getMaterialId()));
        assertThat(actual.getDocumentName(), is(expected.getDocumentName()));
        assertThat(actual.getPublishedDate(), is(expected.getPublishedDate()));
        assertThat(actual.getPageCount(), is(expected.getPageCount()));
        assertThat(actual.getSize(), is(expected.getSize()));
    }

    private DefenceClient getDefenceClient1(final UUID caseId) {
        final String defenceClientOneFirstName = "TEST ONE FIRST NAME";
        final String defenceClientOneLastName = "TEST ONE LAST NAME";
        final LocalDate defenceClientOneDob = of(1985, 10, 21);

        return new DefenceClient(randomUUID(), defenceClientOneFirstName, defenceClientOneLastName, caseId, defenceClientOneDob, randomUUID());
    }
}
