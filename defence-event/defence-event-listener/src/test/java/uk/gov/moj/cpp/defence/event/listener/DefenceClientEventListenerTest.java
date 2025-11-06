package uk.gov.moj.cpp.defence.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.cps.defence.DefendantDetails.defendantDetails;

import uk.gov.justice.cps.defence.DefendantDetails;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.defence.Organisation;
import uk.gov.moj.cpp.defence.common.util.GenericEnveloper;
import uk.gov.moj.cpp.defence.event.listener.events.DefendantUpdateReceived;
import uk.gov.moj.cpp.defence.events.DefenceClientReceived;
import uk.gov.moj.cpp.defence.events.DefendantDefenceAssociationLockedForLaa;
import uk.gov.moj.cpp.defence.persistence.DefenceClientRepository;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefenceClientEventListenerTest {

    private static final LocalDate DOB = LocalDate.of(1985, 10, 23);

    private static final String FIRST_NAME = "BRIAN";

    private static final String LAST_NAME = "BURGLAR";

    private static final String UPDATED_ORGANISATION_NAME = "Updated-Organisation";

    private static final UUID DEFENCE_CLIENT_ID = randomUUID();

    private static final String URN = "55DP0028116";

    private static final UUID DEFENDANT_ID = randomUUID();

    private static final String LAA_CONTRACT_NUMBER = "AB1234";


    @Mock
    private DefenceClientRepository defenceClientRepositoryMock;

    @Captor
    private ArgumentCaptor<DefenceClient> argumentCaptor;

    @InjectMocks
    private DefenceClientEventListener defenceClientEventListener;

    private final GenericEnveloper genericEnveloper = new GenericEnveloper();

    @Test
    public void shouldInvokeDefenceClientRepositorySaveWithCorrectArgs() {

        final UUID caseId = randomUUID();
        final Envelope<DefenceClientReceived> envelope = createEnvelope(caseId);

        defenceClientEventListener.defenceClientReceived(envelope);

        verify(defenceClientRepositoryMock).save(argumentCaptor.capture());
        assertEquals(DEFENCE_CLIENT_ID, argumentCaptor.getValue().getId());
        assertEquals(FIRST_NAME, argumentCaptor.getValue().getFirstName());
        assertEquals(LAST_NAME, argumentCaptor.getValue().getLastName());
        assertEquals(caseId, argumentCaptor.getValue().getCaseId());
        assertEquals(DOB, argumentCaptor.getValue().getDateOfBirth());
        assertEquals(DEFENDANT_ID,argumentCaptor.getValue().getDefendantId());
    }

    @Test
    public void shouldInvokeDefenceClientRepositorySaveWithUpdatedValuesForIndividual() {

        final UUID caseId = randomUUID();
        final Envelope<DefendantUpdateReceived> envelope = createEnvelopeForUpdateIndividualDefenceClient(caseId);

        final DefenceClient existingDefenceClientEntity = new DefenceClient();
        existingDefenceClientEntity.setFirstName("Name1");
        existingDefenceClientEntity.setLastName("LastName1");
        existingDefenceClientEntity.setDateOfBirth(DOB);
        existingDefenceClientEntity.setOrganisationName(null);
        existingDefenceClientEntity.setCaseId(caseId);
        existingDefenceClientEntity.setDefendantId(DEFENDANT_ID);
        existingDefenceClientEntity.setId(DEFENCE_CLIENT_ID);

        when(defenceClientRepositoryMock.findOptionalByDefendantIdAndCaseId(DEFENDANT_ID,caseId)).thenReturn(existingDefenceClientEntity);

        defenceClientEventListener.defenceUpdateReceived(envelope);

        verify(defenceClientRepositoryMock).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getFirstName(),is(FIRST_NAME));
        assertThat(argumentCaptor.getValue().getLastName(), is(LAST_NAME));
        assertThat(argumentCaptor.getValue().getDateOfBirth(), is(DOB));
        assertThat(argumentCaptor.getValue().getId(),is(DEFENCE_CLIENT_ID));
        assertThat(argumentCaptor.getValue().getCaseId(),is(caseId));
        assertThat(argumentCaptor.getValue().getDefendantId(),is(DEFENDANT_ID));
        assertThat(argumentCaptor.getValue().getOrganisationName(),nullValue());

    }



    @Test
    public void shouldInvokeDefenceClientRepositorySaveWithUpdatedValuesForCorporate() {

        final UUID caseId = randomUUID();
        final Envelope<DefendantUpdateReceived> envelope = createEnvelopeForUpdateCorporateDefenceClient(caseId);

        final DefenceClient existingDefenceClientEntity = new DefenceClient();
        existingDefenceClientEntity.setOrganisationName("");
        existingDefenceClientEntity.setCaseId(caseId);
        existingDefenceClientEntity.setDefendantId(DEFENDANT_ID);
        existingDefenceClientEntity.setId(DEFENCE_CLIENT_ID);

        when(defenceClientRepositoryMock.findOptionalByDefendantIdAndCaseId(DEFENDANT_ID,caseId)).thenReturn(existingDefenceClientEntity);

        defenceClientEventListener.defenceUpdateReceived(envelope);

        verify(defenceClientRepositoryMock).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getFirstName(),nullValue());
        assertThat(argumentCaptor.getValue().getLastName(), nullValue());
        assertThat(argumentCaptor.getValue().getDateOfBirth(), nullValue());
        assertThat(argumentCaptor.getValue().getId(),is(DEFENCE_CLIENT_ID));
        assertThat(argumentCaptor.getValue().getCaseId(),is(caseId));
        assertThat(argumentCaptor.getValue().getDefendantId(),is(DEFENDANT_ID));
        assertThat(argumentCaptor.getValue().getOrganisationName(),is(UPDATED_ORGANISATION_NAME));
    }

    @Test
    public void shouldNotInvokeDefenceClientRepositorySaveWhenDefenceClientISNotThere() {

        final UUID caseId = randomUUID();
        final Envelope<DefendantUpdateReceived> envelope = createEnvelopeForUpdateCorporateDefenceClient(caseId);


        when(defenceClientRepositoryMock.findOptionalByDefendantIdAndCaseId(DEFENDANT_ID,caseId)).thenReturn(null);

        defenceClientEventListener.defenceUpdateReceived(envelope);

        verify(defenceClientRepositoryMock, never()).save(argumentCaptor.capture());

    }


    private Envelope<DefendantUpdateReceived> createEnvelopeForUpdateIndividualDefenceClient(final UUID caseId) {
        final Metadata metadata = getMetaData();

        final DefendantDetails defendantDetails = DefendantDetails.defendantDetails()
                .withFirstName(FIRST_NAME)
                .withLastName(LAST_NAME)
                .withDateOfBirth(DOB.toString())
                .withCaseId(caseId)
                .withId(DEFENDANT_ID)
                .build();

        final DefendantUpdateReceived defendantUpdateReceived = DefendantUpdateReceived.defendantUpdateReceived()
                .withDefendantId(DEFENDANT_ID)
                .withDefendantDetails(defendantDetails).build();

        return genericEnveloper.envelopeWithNewActionName(defendantUpdateReceived, metadata, "actionName");
    }



    private Envelope<DefendantUpdateReceived> createEnvelopeForUpdateCorporateDefenceClient(final UUID caseId) {
        final Metadata metadata = getMetaData();

        final Organisation organisation = Organisation.organisation().withOrganisationName(UPDATED_ORGANISATION_NAME).build();

        final DefendantDetails defendantDetails = DefendantDetails.defendantDetails()
                .withOrganisation(organisation)
                .withCaseId(caseId)
                .withId(DEFENDANT_ID)
                .build();

        final DefendantUpdateReceived defendantUpdateReceived = DefendantUpdateReceived.defendantUpdateReceived()
                .withDefendantId(DEFENDANT_ID)
                .withDefendantDetails(defendantDetails).build();

        return genericEnveloper.envelopeWithNewActionName(defendantUpdateReceived, metadata, "actionName");
    }

    private Envelope<DefendantDefenceAssociationLockedForLaa> createEnvelopeForUpdateLaaRepOrderForDefenceClient(final UUID caseId) {

        final Metadata metadata = getMetaData();

        final DefendantDefenceAssociationLockedForLaa defendantDefenceAssociationLockedForLaa = DefendantDefenceAssociationLockedForLaa.defendantDefenceAssociationLockedForLaa()
                .withDefendantId(DEFENDANT_ID)
                .withLaaContractNumber(LAA_CONTRACT_NUMBER).build();

        return genericEnveloper.envelopeWithNewActionName(defendantDefenceAssociationLockedForLaa, metadata, "actionName");

    }

    private Envelope<DefenceClientReceived> createEnvelope(final UUID caseId) {
        final Metadata metadata = getMetaData();

        final DefenceClientReceived defenceClientReceived = DefenceClientReceived.defenceClientReceived()
                .withDefenceClientId(DEFENCE_CLIENT_ID)
                .withUrn(URN)
                .withDefendantId(DEFENDANT_ID)
                .withDefendantDetails(defendantDetails()
                        .withLastName(LAST_NAME)
                        .withFirstName(FIRST_NAME)
                        .withDateOfBirth(DOB.toString())
                        .withCaseId(caseId)
                        .build())
                .build();

        return genericEnveloper.envelopeWithNewActionName(defenceClientReceived, metadata, "actionName");
    }

    public Metadata getMetaData() {
        return Envelope.metadataBuilder()
                .withId(randomUUID())
                .withName("actionName")
                .createdAt(ZonedDateTime.now())
                .build();
    }
}
