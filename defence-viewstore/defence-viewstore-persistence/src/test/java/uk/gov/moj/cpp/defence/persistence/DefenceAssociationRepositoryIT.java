package uk.gov.moj.cpp.defence.persistence;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceAssociation;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceAssociationDefendant;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class DefenceAssociationRepositoryIT {

    private static final String PERSISTENCE_UNIT = "defence-test-persistence-unit";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private DefenceAssociationRepository defenceAssociationRepository;
    private DefenceAssociationDefendantRepository defenceAssociationDefendantRepository;

    @BeforeEach
    public void setUpRepositories() {
        defenceAssociationRepository = new DefenceAssociationRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(defenceAssociationRepository);
        defenceAssociationDefendantRepository = new DefenceAssociationDefendantRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(defenceAssociationDefendantRepository);
    }

    @Test
    public void testFindByUserIdAndCurrentDate() {

        UUID userId = UUID.randomUUID();
        DefenceAssociation defenceAssociation = new DefenceAssociation();
        defenceAssociation.setId(UUID.randomUUID());
        defenceAssociation.setUserId(userId);
        ZonedDateTime dateTIme = ZonedDateTime.now();
        defenceAssociation.setStartDate(dateTIme);
        defenceAssociation.setEndDate(dateTIme);
        DefenceAssociationDefendant defenceAssociationDefendant = new DefenceAssociationDefendant();
        defenceAssociationDefendant.setDefendantId(UUID.randomUUID());
        defenceAssociation.setDefenceAssociationDefendant(defenceAssociationDefendant);
        defenceAssociationDefendant.getDefenceAssociations().add(defenceAssociation);
        DefenceAssociationDefendant dbEntity = defenceAssociationDefendantRepository.save(defenceAssociationDefendant);
        Assertions.assertNotNull(dbEntity);
        Assertions.assertEquals(1, dbEntity.getDefenceAssociations().size());

        final List<DefenceAssociation> defenceAssociationList = defenceAssociationRepository.findByUserIdAndCurrentDate(userId, dateTIme);

        if (!defenceAssociationList.isEmpty()) {
            DefenceAssociation defenceAssociationDbEntity = defenceAssociationList.get(0);
            Assertions.assertNotNull(defenceAssociationDbEntity);
            Assertions.assertEquals(userId, defenceAssociationDbEntity.getUserId());
        }
    }
}
