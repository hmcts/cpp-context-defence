package uk.gov.moj.cpp.defence.persistence;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;
import uk.gov.moj.cpp.defence.persistence.entity.DefendantAllocation;
import uk.gov.moj.cpp.defence.persistence.entity.DefendantAllocationPlea;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;

@RunWith(CdiTestRunner.class)
public class DefendantAllocationRepositoryIT extends BaseTransactionalTest {

    @Inject
    DefendantAllocationRepository defendantAllocationRepository;

    @Inject
    DefenceClientRepository defenceClientRepository;

    final UUID caseId= randomUUID();
    final UUID defendantId= randomUUID();
    final UUID offenceId1 = randomUUID();
    final UUID offenceId2 = randomUUID();

    @Test
    public void shouldAddPleaInDB() {
        DefendantAllocation defendantAllocation = createDefendantAllocation(defendantId);
        defendantAllocationRepository.save(defendantAllocation);

        DefendantAllocation found = defendantAllocationRepository.findBy(defendantAllocation.getId());
        List<DefendantAllocationPlea> pleas=found.getDefendantAllocationPleas();
        Assert.assertSame(defendantAllocation.getId(), found.getId());
        Assert.assertSame(defendantAllocation.getAcknowledgement(), found.getAcknowledgement());
        Assert.assertSame(defendantAllocation.getDefendantId(), found.getDefendantId());
        Assert.assertEquals(2,pleas.size());
        Assert.assertEquals(pleas.get(0).getOffenceId(), offenceId1);
        Assert.assertEquals(pleas.get(1).getOffenceId(), offenceId2);
    }

    @Test
    public void shouldUpdatePleaInDB() {
        //create
        DefendantAllocation defendantAllocation = createDefendantAllocation(defendantId);
        defendantAllocationRepository.save(defendantAllocation);

        DefendantAllocation found = defendantAllocationRepository.findBy(defendantAllocation.getId());
        Assert.assertNull(found.getDefendantAllocationPleas().get(0).getPleaDate());

        //updated
        found.getDefendantAllocationPleas().get(0).setPleaDate(LocalDate.now());
        defendantAllocationRepository.save(found);

        //verify updated
        DefendantAllocation foundAfterUpdate = defendantAllocationRepository.findBy(defendantAllocation.getId());
        Assert.assertNotNull(foundAfterUpdate.getDefendantAllocationPleas().get(0).getPleaDate());

        Assert.assertSame(defendantAllocation.getId(), foundAfterUpdate.getId());
        Assert.assertSame(defendantAllocation.getAcknowledgement(), foundAfterUpdate.getAcknowledgement());
        Assert.assertSame(defendantAllocation.getDefendantId(), foundAfterUpdate.getDefendantId());

    }

    @Test
    public void shouldFindPleasByCaseId(){

        DefenceClient defenceClient = new DefenceClient();
        defenceClient.setDefendantId(defendantId);
        defenceClient.setCaseId(caseId);
        defenceClient.setId(randomUUID());
        defenceClientRepository.save(defenceClient);

        DefendantAllocation defendantAllocation = createDefendantAllocation(defendantId);
        defendantAllocationRepository.save(defendantAllocation);

        List<DefendantAllocation> pleasForCase = defendantAllocationRepository.findDefendantAllocationByCaseId(caseId);
        Assert.assertNotNull(pleasForCase);
        Assert.assertEquals(defendantId,pleasForCase.get(0).getDefendantId());
    }

    private DefendantAllocation createDefendantAllocation(UUID defendantId) {
        DefendantAllocation defendantAllocation = new DefendantAllocation();

        final List<DefendantAllocationPlea> defendantAllocationPleaList = new ArrayList<>();
        final LocalDate pleaDate= null;
        final String indicatedPlea="NOTGUILTY";
        final DefendantAllocationPlea allocationPlea = new DefendantAllocationPlea(offenceId1, pleaDate, indicatedPlea, defendantAllocation);

        defendantAllocationPleaList.add(allocationPlea);

        final DefendantAllocationPlea allocationPlea2 = new DefendantAllocationPlea(offenceId2, pleaDate, indicatedPlea, defendantAllocation);
        defendantAllocationPleaList.add(allocationPlea2);

        defendantAllocation.setId(randomUUID());
        defendantAllocation.setDefendantId(defendantId);
        defendantAllocation.setDefendantAllocationPleas(defendantAllocationPleaList);

        return defendantAllocation;
    }


}
