package uk.gov.moj.cpp.defence.persistence;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceCase;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class DefenceCaseRepositoryIT {

    private static final String PERSISTENCE_UNIT = "defence-test-persistence-unit";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private DefenceCaseRepository defenceCaseRepository;

    @BeforeEach
    public void setUpRepositories() {
        defenceCaseRepository = new DefenceCaseRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(defenceCaseRepository);
    }

    @Test
    public void findIdpcDetailsForDefenceClientId() {

        UUID caseId = randomUUID();
        String urn = "TEST URN";

        DefenceCase defenceCase = new DefenceCase();
        defenceCase.setId(caseId);
        defenceCase.setUrn(urn);

        defenceCaseRepository.save(defenceCase);

        DefenceCase savedCase = defenceCaseRepository.findBy(caseId);
        assertThat(savedCase.getId(), is(caseId));
        assertThat(savedCase.getUrn(), is(urn));

        savedCase = defenceCaseRepository.findOptionalByUrn(urn);
        assertThat(savedCase.getId(), is(caseId));
        assertThat(savedCase.getUrn(), is(urn));
    }

    @Test
    public void shouldReturnNullWhenDefenceClientNotKnown() {
        DefenceCase random = defenceCaseRepository.findBy(UUID.randomUUID());
        assertThat(random, is(nullValue()));

        random = defenceCaseRepository.findOptionalByUrn("random");
        assertThat(random, is(nullValue()));
    }
}
