package uk.gov.moj.cpp.defence.persistence;

import uk.gov.moj.cpp.defence.persistence.entity.DefenceAssociation;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceAssociationDefendant;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class DefenceAssociationRepositoryIT {
    @Inject
    private DefenceAssociationRepository defenceAssociationRepository;
    @Inject
    private DefenceAssociationDefendantRepository defenceAssociationDefendantRepository;

    @Test
    public void testFindByUserIdAndCurrentDate(){

        UUID userId=UUID.randomUUID();
        DefenceAssociation defenceAssociation=new DefenceAssociation();
        defenceAssociation.setId(UUID.randomUUID());
        defenceAssociation.setUserId(userId);
        ZonedDateTime dateTIme=ZonedDateTime.now();
        defenceAssociation.setStartDate(dateTIme);
        defenceAssociation.setEndDate(dateTIme);
        DefenceAssociationDefendant defenceAssociationDefendant=new DefenceAssociationDefendant();
        defenceAssociationDefendant.setDefendantId(UUID.randomUUID());
        defenceAssociationDefendant.getDefenceAssociations().add(defenceAssociation);
        DefenceAssociationDefendant dbEntity=defenceAssociationDefendantRepository.save(defenceAssociationDefendant);
        Assert.assertNotNull(dbEntity);
        Assert.assertEquals(1,dbEntity.getDefenceAssociations().size());

        final List<DefenceAssociation> defenceAssociationList = defenceAssociationRepository.findByUserIdAndCurrentDate(userId, dateTIme);

        if(!defenceAssociationList.isEmpty()) {
            DefenceAssociation defenceAssociationDbEntity = defenceAssociationList.get(0);
            Assert.assertNotNull(defenceAssociationDbEntity);
            Assert.assertEquals(userId, defenceAssociationDbEntity.getUserId());
        }
    }
}
