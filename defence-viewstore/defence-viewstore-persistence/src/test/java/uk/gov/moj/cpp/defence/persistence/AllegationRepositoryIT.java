package uk.gov.moj.cpp.defence.persistence;

import static java.time.LocalDate.now;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.defence.builder.DefenceClientBuilder.createDefenceClient;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalJunit4Test;
import uk.gov.moj.cpp.defence.persistence.entity.Allegation;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class AllegationRepositoryIT extends BaseTransactionalJunit4Test {

    @Inject
    AllegationRepository allegationRepository;

    @Inject
    DefenceClientRepository defenceClientRepository;

    @Test
    public void shouldFindAllegationsByDefenceClientId() {

        final DefenceClient defClient = createDefenceClient();

        Allegation allegation = createAllegation(defClient);
        defClient.getAllegationList().add(allegation);
        defenceClientRepository.save(defClient);
        allegationRepository.save(allegation);

        insertOtherDefenceClientAndAllegationData();

        final List<Allegation> allegations = allegationRepository.findAllegationByCriteria(defClient.getId());

        assertThat(allegations.size(), is(1));
        final Allegation savedAllegation = allegations.get(0);
        assertThat(savedAllegation.getId(), is(allegation.getId()));
        assertThat(savedAllegation.getOffenceId(), is(allegation.getOffenceId()));
        assertThat(savedAllegation.getLegislation(), is(allegation.getLegislation()));
        assertThat(savedAllegation.getTitle(), is(allegation.getTitle()));
        assertThat(savedAllegation.getChargeDate(), is(now()));
    }

    @Test
    public void shouldFindAllegationsByDefenceClientIdAndOffenceId() {

        final DefenceClient defClient = createDefenceClient();

        Allegation allegation = createAllegation(defClient);
        defClient.getAllegationList().add(allegation);
        defenceClientRepository.save(defClient);
        allegationRepository.save(allegation);

        insertOtherDefenceClientAndAllegationData();

        final Allegation savedAllegation = allegationRepository.findAllegationByDefenceClientIdAndOffenceId(defClient.getId(), allegation.getOffenceId());

        assertThat(savedAllegation.getId(), is(allegation.getId()));
        assertThat(savedAllegation.getOffenceId(), is(allegation.getOffenceId()));
        assertThat(savedAllegation.getLegislation(), is(allegation.getLegislation()));
        assertThat(savedAllegation.getTitle(), is(allegation.getTitle()));
        assertThat(savedAllegation.getChargeDate(), is(now()));
    }

    private void insertOtherDefenceClientAndAllegationData() {

        DefenceClient defClient = createDefenceClient();

        Set<Allegation> allegationList = new HashSet<>();
        allegationList.add(createAllegation(defClient));
        allegationList.add(createAllegation(defClient));
        allegationList.add(createAllegation(defClient));
        defClient.setAllegationList(allegationList);
        defenceClientRepository.save(defClient);
        allegationList.forEach(a -> {allegationRepository.save(a);});



        defClient = createDefenceClient();
        defClient.setVisible(false);
        allegationList = new HashSet<>();
        allegationList.add(createAllegation(defClient));
        allegationList.add(createAllegation(defClient));
        allegationList.add(createAllegation(defClient));
        allegationList.add(createAllegation(defClient));
        defClient.setAllegationList(allegationList);
        defenceClientRepository.save(defClient);
        allegationList.forEach(a -> {allegationRepository.save(a);});
    }


    @Test
    public void shouldNotReturnAnyAllegationsWhenDefenceClientMarkedNotVisible() {

        final DefenceClient defClient = createDefenceClient();
        defClient.setVisible(false);
        Allegation allegation = createAllegation(defClient);
        defClient.getAllegationList().add(allegation);
        defenceClientRepository.save(defClient);
        allegationRepository.save(allegation);

        insertOtherDefenceClientAndAllegationData();

        final List<Allegation> allegations = allegationRepository.findAllegationByCriteria(defClient.getId());

        assertThat(allegations.size(), is(0));
    }

    private Allegation createAllegation(final DefenceClient defClient) {
        final UUID allegationId = UUID.randomUUID();
        final String legislation = "s18, Offences Against the Person Act 1861";
        final String title = "Cause grievous bodily harm with intent";
        final UUID offenceId = UUID.randomUUID();
        final LocalDate chargeDate = now();

        return new Allegation(allegationId, defClient.getId(),offenceId, legislation, title, chargeDate);
    }
}