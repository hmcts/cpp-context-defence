package uk.gov.moj.cpp.defence.persistence;

import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.defence.builder.DefenceClientBuilder.createDefenceClient;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalJunit4Test;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceGrantAccess;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceUserDetails;
import uk.gov.moj.cpp.defence.persistence.entity.OrganisationDetails;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class DefenceGrantAccessRepositoryIT  extends BaseTransactionalJunit4Test {

    @Inject
    DefenceGrantAccessRepository defenceGrantAccessRepository;

    @Inject
    DefenceClientRepository defenceClientRepository;

    @Test
    public void shouldFindByDefenceGrantAccessId() {

        final DefenceClient defClient = createDefenceClient();

        defenceClientRepository.save(defClient);

        UUID userId = randomUUID();
        UUID grantorUserId = randomUUID();
        UUID organisationId = randomUUID();


        OrganisationDetails organisationDetails = new OrganisationDetails(randomUUID(), organisationId, "Test Ltd");
        DefenceGrantAccess defenceGrantAccess = new DefenceGrantAccess();
        defenceGrantAccess.setDefenceClient(defClient);
        defenceGrantAccess.setId(randomUUID());
        defenceGrantAccess.setStartDate(ZonedDateTime.now());
        defenceGrantAccess.setGranteeDefenceUserDetails(new DefenceUserDetails(randomUUID(), userId, "John", "Trackey"));
        defenceGrantAccess.setGrantorDefenceUserDetails(new DefenceUserDetails(randomUUID(), grantorUserId, "Tim", "Quick"));
        defenceGrantAccess.setGranteeOrganisationDetails(organisationDetails);


        defenceGrantAccessRepository.save(defenceGrantAccess);

        final DefenceGrantAccess defenceGrantAccessResult = defenceGrantAccessRepository.findBy(defenceGrantAccess.getId());

        assertThat(defenceGrantAccessResult, notNullValue());
        assertThat(defenceGrantAccessResult.getId(), is(defenceGrantAccess.getId()));
        assertThat(defenceGrantAccessResult.getGranteeDefenceUserDetails().getUserId(), is(defenceGrantAccess.getGranteeDefenceUserDetails().getUserId()));
        assertThat(defenceGrantAccessResult.getGrantorDefenceUserDetails().getUserId(), is(defenceGrantAccess.getGrantorDefenceUserDetails().getUserId()));
        assertThat(defenceGrantAccessResult.getGranteeOrganisationDetails().getOrganisationId(), is(defenceGrantAccess.getGranteeOrganisationDetails().getOrganisationId()));
    }

    @Test
    public void shouldFindByActiveDefenceClientId() {

        final DefenceClient defClient = createDefenceClient();
        defenceClientRepository.save(defClient);

        UUID granteeUserId = randomUUID();
        UUID grantorUserId = randomUUID();
        UUID organisationId = randomUUID();

        final DefenceGrantAccess defenceGrantAccess = getDefenceGrantAccess(defClient, granteeUserId, grantorUserId, organisationId, false);
        defenceGrantAccessRepository.save(defenceGrantAccess);

        final DefenceGrantAccess defenceGrantAccess1 = getDefenceGrantAccess(defClient, granteeUserId, grantorUserId, organisationId, true);
        defenceGrantAccessRepository.save(defenceGrantAccess1);

        UUID granteeUserId1 = randomUUID();
        UUID grantorUserId1 = randomUUID();
        UUID organisationId1 = randomUUID();

        final DefenceGrantAccess defenceGrantAccess2 = getDefenceGrantAccess(defClient, granteeUserId1, grantorUserId1, organisationId1, false);
        defenceGrantAccessRepository.save(defenceGrantAccess2);

        final List<DefenceGrantAccess> defenceGrantAccessResultList = defenceGrantAccessRepository.findByDefenceClient(defClient.getId());

        assertThat(nonNull(defenceGrantAccessResultList) && !defenceGrantAccessResultList.isEmpty(), is(true));

        DefenceGrantAccess defenceGrantAccessResult = defenceGrantAccessResultList.get(0);
        assertThat(defenceGrantAccessResult.getId(), is(defenceGrantAccess.getId()));
        assertThat(defenceGrantAccessResult.getGranteeDefenceUserDetails().getUserId(), is(defenceGrantAccess.getGranteeDefenceUserDetails().getUserId()));
        assertThat(defenceGrantAccessResult.getGrantorDefenceUserDetails().getUserId(), is(defenceGrantAccess.getGrantorDefenceUserDetails().getUserId()));
        assertThat(defenceGrantAccessResult.getGranteeOrganisationDetails().getOrganisationId(), is(defenceGrantAccess.getGranteeOrganisationDetails().getOrganisationId()));
    }

    @Test
    public void shouldFindByCaseIdAndGranteeUserId() {

        final DefenceClient defClient = createDefenceClient();
        defenceClientRepository.save(defClient);

        UUID granteeUserId = randomUUID();
        UUID grantorUserId = randomUUID();
        UUID organisationId = randomUUID();

        final DefenceGrantAccess defenceGrantAccess = getDefenceGrantAccess(defClient, granteeUserId, grantorUserId, organisationId, false);
        defenceGrantAccessRepository.save(defenceGrantAccess);

        final DefenceGrantAccess defenceGrantAccess1 = getDefenceGrantAccess(defClient, granteeUserId, grantorUserId, organisationId, true);
        defenceGrantAccessRepository.save(defenceGrantAccess1);

        UUID granteeUserId1 = randomUUID();
        UUID grantorUserId1 = randomUUID();
        UUID organisationId1 = randomUUID();

        final DefenceGrantAccess defenceGrantAccess2 = getDefenceGrantAccess(defClient, granteeUserId1, grantorUserId1, organisationId1, false);
        defenceGrantAccessRepository.save(defenceGrantAccess2);

        final List<DefenceGrantAccess> defenceGrantAccessResults = defenceGrantAccessRepository.findByGranteeAndCaseId(defClient.getCaseId(), granteeUserId);

        assertThat(defenceGrantAccessResults, notNullValue());
        assertThat(defenceGrantAccessResults.get(0).getId(), is(defenceGrantAccess.getId()));
        assertThat(defenceGrantAccessResults.get(0).getGranteeDefenceUserDetails().getUserId(), is(defenceGrantAccess.getGranteeDefenceUserDetails().getUserId()));
        assertThat(defenceGrantAccessResults.get(0).getGrantorDefenceUserDetails().getUserId(), is(defenceGrantAccess.getGrantorDefenceUserDetails().getUserId()));
        assertThat(defenceGrantAccessResults.get(0).getGranteeOrganisationDetails().getOrganisationId(), is(defenceGrantAccess.getGranteeOrganisationDetails().getOrganisationId()));
    }

    private DefenceGrantAccess getDefenceGrantAccess(final DefenceClient defClient, final UUID granteeUserId, final UUID grantorUserId, final UUID organisationId, final boolean remove) {
        final DefenceGrantAccess defenceGrantAccess = new DefenceGrantAccess();
        defenceGrantAccess.setDefenceClient(defClient);
        defenceGrantAccess.setId(randomUUID());
        defenceGrantAccess.setStartDate(ZonedDateTime.now());
        defenceGrantAccess.setGranteeDefenceUserDetails(new DefenceUserDetails(randomUUID(), granteeUserId, "John", "Trackey"));
        defenceGrantAccess.setGrantorDefenceUserDetails(new DefenceUserDetails(randomUUID(), grantorUserId, "Tim", "Quick"));
        defenceGrantAccess.setGranteeOrganisationDetails(new OrganisationDetails(randomUUID(), organisationId, "Test Ltd"));
        defenceGrantAccess.setRemoved(remove);
        return defenceGrantAccess;
    }

}
