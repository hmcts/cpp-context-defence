package uk.gov.moj.cpp.defence.persistence;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.defence.persistence.entity.IdpcAccess;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class IdpcAccessHistoryRepositoryIT {

    private static final String PERSISTENCE_UNIT = "defence-test-persistence-unit";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private IdpcAccessHistoryRepository idpcAccessHistoryRepository;
    private DefenceClientRepository defenceClientRepository;

    private static final UUID DEFENCECLIENT_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();
    private static final UUID IDPC_ID = randomUUID();
    private static final UUID ORGANISATION_ID = randomUUID();
    private static final ZonedDateTime NOW = ZonedDateTime.now(ZoneId.of("UTC"));

    @BeforeEach
    public void setUpRepositories() {
        idpcAccessHistoryRepository = new IdpcAccessHistoryRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(idpcAccessHistoryRepository);
        defenceClientRepository = new DefenceClientRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(defenceClientRepository);
    }

    @Test
    public void shouldSaveIDPCAccess() {
        final IdpcAccess idpcAccess = createStaticIdpcAccess();
        idpcAccessHistoryRepository.save(idpcAccess);

        final List<IdpcAccess> resultList = idpcAccessHistoryRepository.findIdpcAccessByCriteria(DEFENCECLIENT_ID);
        assertThat(resultList.size(), is(1));
        final IdpcAccess idpcAccessSaved = resultList.get(0);

        assertThat(idpcAccessSaved.getId(), is(idpcAccess.getId()));
        assertThat(idpcAccessSaved.getDefenceClientId(), is(idpcAccess.getDefenceClientId()));
        assertThat(idpcAccessSaved.getIdpcDetailsId(), is(idpcAccess.getIdpcDetailsId()));
        assertThat(idpcAccessSaved.getUserId(), is(idpcAccess.getUserId()));
        assertThat(idpcAccessSaved.getOrganisationId(), is(idpcAccess.getOrganisationId()));
        assertThat(idpcAccessSaved.getAccessTimestamp(), is(idpcAccess.getAccessTimestamp()));
    }

    @Test
    public void shouldSaveMultipleIDPCAccesses() {
        final IdpcAccess idpcAccess1 = createRandomIdpcAccessFor(DEFENCECLIENT_ID);
        final IdpcAccess idpcAccess2 = createRandomIdpcAccessFor(DEFENCECLIENT_ID);
        idpcAccessHistoryRepository.save(idpcAccess1);
        idpcAccessHistoryRepository.save(idpcAccess2);

        final List<IdpcAccess> resultList = idpcAccessHistoryRepository.findIdpcAccessByCriteria(DEFENCECLIENT_ID);
        assertThat(resultList.size(), is(2));

        assertThat(resultList.stream().map(IdpcAccess::getId).toList(), hasItems(idpcAccess1.getId(), idpcAccess2.getId()));
    }

    @Test
    public void testAccessFromOrganisationByDifferentUsers() throws InterruptedException {
        List<UUID> organisations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            UUID orgId = UUID.randomUUID();
            for (int j = 0; j < 5; j++) {
                IdpcAccess idpc = new IdpcAccess(randomUUID(), DEFENCECLIENT_ID, IDPC_ID,
                        randomUUID(), orgId, ZonedDateTime.now(ZoneId.of("UTC")));
                idpcAccessHistoryRepository.save(idpc);
                Thread.sleep(1);
            }
            organisations.add(0, orgId);
        }

        final List<UUID> resultList = idpcAccessHistoryRepository.findOrderedDistinctOrgIdsOfIdpcAccessForDefenceClient(DEFENCECLIENT_ID);

        assertThat(resultList, is(organisations));
    }

    private IdpcAccess createStaticIdpcAccess() {
        return new IdpcAccess(randomUUID(), DEFENCECLIENT_ID, IDPC_ID, USER_ID, ORGANISATION_ID, NOW);
    }

    private IdpcAccess createRandomIdpcAccessFor(final UUID defenceClientId) {
        return createIdpcAccessEntity(defenceClientId, randomUUID(), randomUUID(), randomUUID(), ZonedDateTime.now(ZoneId.of("UTC")));
    }

    private IdpcAccess createIdpcAccessEntity(final UUID defenceClientId, final UUID idpcId, final UUID userId, final UUID orgId, ZonedDateTime timestamp) {
        return new IdpcAccess(randomUUID(), defenceClientId, idpcId, userId, orgId, timestamp);
    }
}
