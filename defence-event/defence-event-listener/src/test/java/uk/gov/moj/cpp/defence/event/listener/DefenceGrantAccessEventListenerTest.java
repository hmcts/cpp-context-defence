package uk.gov.moj.cpp.defence.event.listener;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.defence.common.util.GrantAccessUtil.preparePermissionList;

import uk.gov.justice.cps.defence.PersonDetails;
import uk.gov.justice.cps.defence.events.AccessGrantRemoved;
import uk.gov.justice.cps.defence.events.AccessGranted;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.defence.Organisation;
import uk.gov.moj.cpp.defence.common.util.GenericEnveloper;
import uk.gov.moj.cpp.defence.persistence.DefenceClientRepository;
import uk.gov.moj.cpp.defence.persistence.DefenceGrantAccessRepository;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceGrantAccess;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefenceGrantAccessEventListenerTest {

    @Mock
    private DefenceGrantAccessRepository defenceGrantAccessRepository;

    @Mock
    private DefenceClientRepository defenceClientRepository;

    @InjectMocks
    private DefenceGrantAccessEventListener defenceGrantAccessEventListener;

    @Captor
    private ArgumentCaptor<DefenceGrantAccess> argumentCaptor;

    private final GenericEnveloper genericEnveloper = new GenericEnveloper();

    @Test
    public void defenceGrantAccess() {

        final UUID userId = randomUUID();
        final UUID defenceClientId = randomUUID();
        final Envelope<AccessGranted> envelope = createEnvelopeForGrantAccess(defenceClientId, userId);
        final AccessGranted accessGranted = envelope.payload();

        DefenceClient defenceClient = new DefenceClient();
        defenceClient.setId(defenceClientId);
        when(defenceClientRepository.findOptionalBy(any())).thenReturn(of(defenceClient));

        defenceGrantAccessEventListener.defenceEventAccessGranted(envelope);

        verify(defenceGrantAccessRepository).save(argumentCaptor.capture());

        final DefenceGrantAccess defenceGrantAccess = argumentCaptor.getValue();

        assertThat(defenceGrantAccess.getDefenceClient().getId(), is(accessGranted.getPermissions().get(0).getTarget()));
        assertThat(defenceGrantAccess.getGranteeDefenceUserDetails().getUserId(), is(accessGranted.getGranteeDetails().getUserId()));
        assertThat(defenceGrantAccess.getGranteeDefenceUserDetails().getFirstName(), is(accessGranted.getGranteeDetails().getFirstName()));
        assertThat(defenceGrantAccess.getGranteeDefenceUserDetails().getLastName(), is(accessGranted.getGranteeDetails().getLastName()));

        assertThat(defenceGrantAccess.getGrantorDefenceUserDetails().getUserId(), is(accessGranted.getGranterDetails().getUserId()));
        assertThat(defenceGrantAccess.getGrantorDefenceUserDetails().getFirstName(), is(accessGranted.getGranterDetails().getFirstName()));
        assertThat(defenceGrantAccess.getGrantorDefenceUserDetails().getLastName(), is(accessGranted.getGranterDetails().getLastName()));
        assertThat(defenceGrantAccess.getGranteeOrganisationDetails().getOrganisationId(), is(accessGranted.getGranteeOrganisation().getOrgId()));
        assertThat(defenceGrantAccess.getGranteeOrganisationDetails().getOrganisationName(), is(accessGranted.getGranteeOrganisation().getOrganisationName()));
        assertThat(defenceGrantAccess.isRemoved(), is(false));
    }

    @Test
    public void noDefenceGrantAccessWhenDefenceClientIsNotThere() {

        final UUID userId = randomUUID();
        final UUID defenceClientId = randomUUID();
        final Envelope<AccessGranted> envelope = createEnvelopeForGrantAccess(defenceClientId, userId);
        final AccessGranted accessGranted = envelope.payload();

        when(defenceClientRepository.findOptionalBy(any())).thenReturn(empty());

        defenceGrantAccessEventListener.defenceEventAccessGranted(envelope);

        verify(defenceGrantAccessRepository, never()).save(argumentCaptor.capture());


    }

    @Test
    public void defenceEventAccessGrantRemoved() {

        final UUID userId = UUID.randomUUID();
        final UUID defendantId = UUID.randomUUID();
        final Envelope<AccessGrantRemoved> envelope = createEnvelopeForRemoveGrantAccess(userId, defendantId);
        final AccessGrantRemoved accessGrantRemoved = envelope.payload();

        DefenceClient defenceClient = new DefenceClient();
        defenceClient.setId(defendantId);

        final DefenceGrantAccess defenceGrantAccessMock = new DefenceGrantAccess();
        defenceGrantAccessMock.setRemoved(false);
        defenceGrantAccessMock.setId(accessGrantRemoved.getPermissions().get(0).getId());

        when(defenceGrantAccessRepository.findByDefenceClient(any(), any())).thenReturn(defenceGrantAccessMock);

        defenceGrantAccessEventListener.defenceEventAccessGrantRemoved(envelope);

        verify(defenceGrantAccessRepository).save(argumentCaptor.capture());
        final DefenceGrantAccess defenceGrantAccess = argumentCaptor.getValue();
        assertThat(defenceGrantAccess.getId(), is(defenceGrantAccessMock.getId()));
        assertThat(defenceGrantAccess.isRemoved(), is(true));
        assertThat(defenceGrantAccess.getEndDate(), notNullValue());

    }

    private Envelope<AccessGranted> createEnvelopeForGrantAccess(final UUID target, final UUID source) {
        final Metadata metadata = getMetaData();

        final Organisation organisation = Organisation.organisation().withOrgId(randomUUID()).withOrganisationName("test").build();
        final PersonDetails personDetails = PersonDetails.personDetails().withUserId(randomUUID()).withFirstName("firstName").withLastName("lastName").build();
        final PersonDetails grantorPersonDetails = PersonDetails.personDetails().withUserId(randomUUID()).withFirstName("grantorfirstName").withLastName("grantorlastName").build();
        final AccessGranted accessGranted = AccessGranted.accessGranted()
                .withPermissions(preparePermissionList(target, source, true, empty()))
                .withGranteeOrganisation(organisation)
                .withGranteeDetails(personDetails)
                .withGranterDetails(grantorPersonDetails)
                .build();


        return genericEnveloper.envelopeWithNewActionName(accessGranted, metadata, "defence.event.access-granted");
    }

    private Envelope<AccessGrantRemoved> createEnvelopeForRemoveGrantAccess(final UUID source, final UUID target) {
        final Metadata metadata = getMetaData();

        final AccessGrantRemoved accessGrantRemoved = AccessGrantRemoved.accessGrantRemoved()
                .withPermissions(preparePermissionList(target,source, true, empty()))
                .build();

        return genericEnveloper.envelopeWithNewActionName(accessGrantRemoved, metadata, "defence.event.access-grant-removed");
    }

    public Metadata getMetaData() {
        return Envelope.metadataBuilder()
                .withId(randomUUID())
                .withName("defence.event.access-granted")
                .createdAt(ZonedDateTime.now())
                .build();
    }

}
