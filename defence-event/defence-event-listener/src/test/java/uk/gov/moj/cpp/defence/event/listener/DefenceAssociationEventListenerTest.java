package uk.gov.moj.cpp.defence.event.listener;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.moj.cpp.defence.events.RepresentationType.REPRESENTATION_ORDER_APPLIED_FOR;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationAssociated;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationDisassociated;
import uk.gov.moj.cpp.defence.events.DefendantDefenceAssociationLockedForLaa;
import uk.gov.moj.cpp.defence.persistence.DefenceAssociationDefendantRepository;
import uk.gov.moj.cpp.defence.persistence.DefenceClientRepository;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceAssociation;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceAssociationDefendant;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefenceAssociationEventListenerTest {

    private static final String DEFENCE_ORGANISATION_ASSOCIATED_EVENT = "defence.event.defence-association-received";
    private static final String ASSOCIATION_LOCKED_FOR_LAA_EVENT = "defence.event.defendant-defence-association-locked-for-laa";
    private static final String DEFENCE_ORGANISATION_DISASSOCIATED_EVENT = "defence.event.defence-disassociation-received";
    private static final String LAA_CONTRACT_NUMBER = "AL23467892";
    private static final UUID USER_ID = randomUUID();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID ORGANISATION_ID = randomUUID();
    private static final String ORGANISATION_NAME = "LLOYDS";
    private static final ZonedDateTime START_DATE = ZonedDateTime.now().minusDays(2);
    private static final ZonedDateTime END_DATE = ZonedDateTime.now();
    private static final UUID CASE_ID = randomUUID();

    @Mock
    private DefenceClientRepository defenceClientRepository;

    @Mock
    private DefenceAssociationDefendantRepository defenceAssociationDefendantRepository;

    @Captor
    private ArgumentCaptor<DefenceClient> argumentCaptor;

    @Captor
    private ArgumentCaptor<DefenceAssociationDefendant> defenceAssociationDefendantArgumentCaptor;

    @InjectMocks
    private DefenceAssociationEventListener eventListener;

    @Test
    public void shouldPersistDefenceAssociation() {
        //Given
        DefenceClient defenceClient = generateDefenceClient(DEFENDANT_ID.toString());
        when(defenceClientRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(defenceClient);

        //When
        eventListener.processOrganisationAssociated(buildAssociatedEvent(ORGANISATION_ID));

        //Then
        verify(defenceClientRepository).save(argumentCaptor.capture());
        verify(defenceAssociationDefendantRepository).save(defenceAssociationDefendantArgumentCaptor.capture());
        final DefenceClient updatedDefenceClient = argumentCaptor.getValue();
        assertThat(updatedDefenceClient.getDefendantId(),is(DEFENDANT_ID));
        assertThat(updatedDefenceClient.getAssociatedOrganisation(), notNullValue());
        assertThat(updatedDefenceClient.getAssociatedOrganisation(), is(ORGANISATION_ID));

        final DefenceAssociationDefendant defenceAssociationDefendant = defenceAssociationDefendantArgumentCaptor.getValue();
        assertThat(DEFENDANT_ID, is(defenceAssociationDefendant.getDefendantId()));

    }

    @Test
    public void shouldRemoveDefenceAssociation() {
        //Given
        DefenceClient defenceClient = generateDefenceClient(DEFENDANT_ID.toString());
        when(defenceClientRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(defenceClient);
        when(defenceAssociationDefendantRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(generateDefenceAssociationDefendant());
        eventListener.processOrganisationAssociated(buildAssociatedEvent(ORGANISATION_ID));
        //When
        eventListener.processOrganisationDisassociated(buildDisassociatedEvent(ORGANISATION_ID));

        //Then
        verify(defenceClientRepository, times(2)).save(argumentCaptor.capture());
        final DefenceClient updatedDefenceClient = argumentCaptor.getAllValues().get(1);
        assertThat(updatedDefenceClient.getDefendantId(), is(DEFENDANT_ID));
        assertThat(updatedDefenceClient.getAssociatedOrganisation(), nullValue());
    }

    @Test
    public void shouldAllowSubsequentDefenceAssociation() {
        //Given
        DefenceClient defenceClient = generateDefenceClient(DEFENDANT_ID.toString());
        when(defenceClientRepository.findOptionalByDefendantId(fromString(DEFENDANT_ID.toString()))).thenReturn(defenceClient);
        when(defenceAssociationDefendantRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(generateDefenceAssociationDefendant());

        eventListener.processOrganisationAssociated(buildAssociatedEvent(ORGANISATION_ID));

        eventListener.processOrganisationDisassociated(buildDisassociatedEvent(ORGANISATION_ID));

        final UUID newOrganisationId = randomUUID();

        eventListener.processOrganisationAssociated(buildAssociatedEvent(newOrganisationId));

        //Then
        verify(defenceClientRepository, times(3)).save(argumentCaptor.capture());
        final DefenceClient updatedDefenceClient = argumentCaptor.getAllValues().get(2);
        assertThat(updatedDefenceClient.getDefendantId(), is(DEFENDANT_ID));
        assertThat(updatedDefenceClient.getAssociatedOrganisation(), notNullValue());
        assertThat(updatedDefenceClient.getAssociatedOrganisation(), is(newOrganisationId));

    }

    @Test
    public void shouldOverwriteExistingOrganisationWhenAlreadyAssociatedWithAnotherOrganisation() {
        //Given
        DefenceClient defenceClient = generateDefenceClient(DEFENDANT_ID.toString());
        when(defenceClientRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(defenceClient);
        when(defenceAssociationDefendantRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(generateDefenceAssociationDefendant());

        eventListener.processOrganisationAssociated(buildAssociatedEvent(ORGANISATION_ID));


        //When
        final UUID organisationTwoId = randomUUID();
        eventListener.processOrganisationAssociated(buildAssociatedEvent(organisationTwoId));

        // Then
        verify(defenceClientRepository, times(2)).save(argumentCaptor.capture());
        final DefenceClient updatedDefenceClient = argumentCaptor.getAllValues().get(1);
        assertThat(updatedDefenceClient.getAssociatedOrganisation(),is(organisationTwoId));
        assertThat(updatedDefenceClient.getLastAssociatedOrganisation(),is(ORGANISATION_ID));
    }

    @Test
    public void shouldPerformNoActionsWhenDisassociatingOrganisationDifferent() {
        //Given
        DefenceClient defenceClient = generateDefenceClient(DEFENDANT_ID.toString());
        when(defenceClientRepository.findOptionalByDefendantId(fromString(DEFENDANT_ID.toString()))).thenReturn(defenceClient);
        when(defenceAssociationDefendantRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(generateDefenceAssociationDefendant());

        eventListener.processOrganisationAssociated(buildAssociatedEvent(ORGANISATION_ID));

        //When
        assertThrows(IllegalArgumentException.class, () -> eventListener.processOrganisationDisassociated(buildDisassociatedEvent(randomUUID())));

        // Then
        verify(defenceClientRepository, times(1)).save(argumentCaptor.capture());
        final DefenceClient updatedDefenceClient = argumentCaptor.getAllValues().get(0);
        assertThat(updatedDefenceClient.getAssociatedOrganisation(),is(ORGANISATION_ID));
    }

    @Test
    public void shouldPersistLastAssociationAfterDisassociation() {
        //Given
        DefenceClient defenceClient = generateDefenceClient(DEFENDANT_ID.toString());
        when(defenceClientRepository.findOptionalByDefendantId(fromString(DEFENDANT_ID.toString()))).thenReturn(defenceClient);
        when(defenceAssociationDefendantRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(generateDefenceAssociationDefendant());

        eventListener.processOrganisationAssociated(buildAssociatedEvent(ORGANISATION_ID));

        //When
        eventListener.processOrganisationDisassociated(buildDisassociatedEvent(ORGANISATION_ID));

        //Then
        verify(defenceClientRepository, times(2)).save(argumentCaptor.capture());
        final DefenceClient updatedDefenceClient = argumentCaptor.getAllValues().get(1);
        assertThat(updatedDefenceClient.getDefendantId(),is(DEFENDANT_ID));
        assertThat(updatedDefenceClient.getAssociatedOrganisation(),nullValue());
        assertThat(updatedDefenceClient.getLastAssociatedOrganisation(),notNullValue());
        assertThat(updatedDefenceClient.getLastAssociatedOrganisation(), is(ORGANISATION_ID));
    }

    @Test
    public void shouldPersistLastAssociationCorrectlyAfterMultipleAssociationsAndDisassociations() {
        //Given
        DefenceClient defenceClient = generateDefenceClient(DEFENDANT_ID.toString());
        when(defenceClientRepository.findOptionalByDefendantId(fromString(DEFENDANT_ID.toString()))).thenReturn(defenceClient);
        when(defenceAssociationDefendantRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(generateDefenceAssociationDefendant());

        //When
        eventListener.processOrganisationAssociated(buildAssociatedEvent(ORGANISATION_ID));
        eventListener.processOrganisationDisassociated(buildDisassociatedEvent(ORGANISATION_ID));

        final UUID newOrganisationId = randomUUID();
        eventListener.processOrganisationAssociated(buildAssociatedEvent(newOrganisationId));
        eventListener.processOrganisationDisassociated(buildDisassociatedEvent(newOrganisationId));

        //Then
        verify(defenceClientRepository, times(4)).save(argumentCaptor.capture());
        final DefenceClient updatedDefenceClient = argumentCaptor.getAllValues().get(3);
        assertThat(updatedDefenceClient.getDefendantId(),is(DEFENDANT_ID));
        assertThat(updatedDefenceClient.getAssociatedOrganisation(),nullValue());
        assertThat(updatedDefenceClient.getLastAssociatedOrganisation(),notNullValue());
        assertThat(updatedDefenceClient.getLastAssociatedOrganisation(),is(newOrganisationId));
    }

    @Test
    public void shouldInvokeDefenceClientRepositorySaveUpdateWithLaaRepOrder() {

        final UUID caseId = randomUUID();
        final Envelope<DefendantDefenceAssociationLockedForLaa> envelope = createEnvelopeForUpdateLaaRepOrderForDefenceClient();
        final DefenceClient existingDefenceClientEntity = new DefenceClient();
        existingDefenceClientEntity.setOrganisationName("ORG1");
        existingDefenceClientEntity.setCaseId(caseId);
        existingDefenceClientEntity.setDefendantId(DEFENDANT_ID);
        existingDefenceClientEntity.setId(UUID.randomUUID());
        existingDefenceClientEntity.setLockedByRepOrder(false);

        final DefenceAssociation defenceAssociation = new DefenceAssociation();
        defenceAssociation.setId(randomUUID());
        final DefenceAssociationDefendant defenceAssociationDefendant = new DefenceAssociationDefendant();
        defenceAssociationDefendant.setDefenceAssociations(newArrayList(defenceAssociation, new DefenceAssociation()));

        when(defenceClientRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(existingDefenceClientEntity);
        when(defenceAssociationDefendantRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(defenceAssociationDefendant);

        eventListener.processDefenceAssociationLockedForLaa(envelope);

        verify(defenceClientRepository).save(argumentCaptor.capture());
        verify(defenceAssociationDefendantRepository).save(defenceAssociationDefendantArgumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getId(), is(existingDefenceClientEntity.getId()));
        assertThat(argumentCaptor.getValue().getDefendantId(), is(existingDefenceClientEntity.getDefendantId()));
        assertThat(argumentCaptor.getValue().getOrganisationName(), is(existingDefenceClientEntity.getOrganisationName()));
        assertThat(argumentCaptor.getValue().getCaseId(), is(existingDefenceClientEntity.getCaseId()));
        assertThat(argumentCaptor.getValue().isLockedByRepOrder(), is(true));

        assertThat(defenceAssociationDefendantArgumentCaptor.getValue().getDefenceAssociations().size(), is(3));
        assertThat(defenceAssociationDefendantArgumentCaptor.getValue().getDefenceAssociations().get(0).getEndDate(), notNullValue());
        assertThat(defenceAssociationDefendantArgumentCaptor.getValue().getDefenceAssociations().get(1).getEndDate(), notNullValue());
        assertThat(defenceAssociationDefendantArgumentCaptor.getValue().getDefenceAssociations().get(2).getEndDate(), nullValue());

    }

    @Test
    public void shouldNotInvokeDefenceClientRepositorySaveUpdateWhenDefenceClientNotThere() {

        final UUID caseId = randomUUID();
        final Envelope<DefendantDefenceAssociationLockedForLaa> envelope = createEnvelopeForUpdateLaaRepOrderForDefenceClient();

        final DefenceAssociation defenceAssociation = new DefenceAssociation();
        defenceAssociation.setId(randomUUID());
        final DefenceAssociationDefendant defenceAssociationDefendant = new DefenceAssociationDefendant();
        defenceAssociationDefendant.setDefenceAssociations(newArrayList(defenceAssociation, new DefenceAssociation()));

        when(defenceClientRepository.findOptionalByDefendantId(DEFENDANT_ID)).thenReturn(null);

        eventListener.processDefenceAssociationLockedForLaa(envelope);

        verify(defenceClientRepository, never()).save(argumentCaptor.capture());
        verify(defenceAssociationDefendantRepository, never()).save(defenceAssociationDefendantArgumentCaptor.capture());
    }

    protected Envelope<DefendantDefenceAssociationLockedForLaa> createEnvelopeForUpdateLaaRepOrderForDefenceClient() {
        final MetadataBuilder metadataBuilder = getMetadataBuilder(ASSOCIATION_LOCKED_FOR_LAA_EVENT);

        DefendantDefenceAssociationLockedForLaa defendantDefenceAssociationLockedForLaa = DefendantDefenceAssociationLockedForLaa.defendantDefenceAssociationLockedForLaa()
                .withDefendantId(DEFENDANT_ID)
                .withLaaContractNumber(LAA_CONTRACT_NUMBER)
                .build();

        return Envelope.envelopeFrom(metadataBuilder, defendantDefenceAssociationLockedForLaa);
    }

    private DefenceClient generateDefenceClient(final String defendantId) {
        DefenceClient defenceClient = new DefenceClient(
                randomUUID(),
                "James",
                "Herriott",
                randomUUID(),
                LocalDate.now(),
                fromString(defendantId)
        );
        defenceClient.setAssociatedOrganisation(randomUUID());

        return defenceClient;
    }

    private DefenceAssociationDefendant generateDefenceAssociationDefendant() {
        DefenceAssociationDefendant defenceAssociationDefendant = new DefenceAssociationDefendant();
        defenceAssociationDefendant.setDefendantId(DEFENDANT_ID);

        DefenceAssociation defenceAssociation = new DefenceAssociation();
        defenceAssociation.setId(randomUUID());
        defenceAssociation.setOrgId(ORGANISATION_ID);
        defenceAssociation.setLaaContractNumber(LAA_CONTRACT_NUMBER);
        defenceAssociation.setRepresentationType(REPRESENTATION_ORDER_APPLIED_FOR.name());
        defenceAssociation.setStartDate(START_DATE);
        defenceAssociation.setEndDate(END_DATE);

        defenceAssociationDefendant.setDefenceAssociations(Stream.of(defenceAssociation).collect(Collectors.toList()));

        return defenceAssociationDefendant;
    }


    protected Envelope<DefenceOrganisationAssociated> buildAssociatedEvent(final UUID organisationId) {
        final MetadataBuilder metadataBuilder = getMetadataBuilder(DEFENCE_ORGANISATION_ASSOCIATED_EVENT);

        DefenceOrganisationAssociated defenceOrganisationAssociated = DefenceOrganisationAssociated.defenceOrganisationAssociated()
                .withDefendantId(DEFENDANT_ID)
                .withLaaContractNumber(LAA_CONTRACT_NUMBER)
                .withOrganisationId(organisationId)
                .withOrganisationName(ORGANISATION_NAME)
                .withRepresentationType(REPRESENTATION_ORDER_APPLIED_FOR)
                .withStartDate(START_DATE)
                .withUserId(USER_ID)
                .build();

        return Envelope.envelopeFrom(metadataBuilder, defenceOrganisationAssociated);
    }

    protected Envelope<DefenceOrganisationDisassociated> buildDisassociatedEvent(final UUID organisationId) {
        final MetadataBuilder metadataBuilder = getMetadataBuilder(DEFENCE_ORGANISATION_DISASSOCIATED_EVENT);

        DefenceOrganisationDisassociated defenceOrganisationDisassociated = DefenceOrganisationDisassociated.defenceOrganisationDisassociated()
                .withDefendantId(DEFENDANT_ID)
                .withOrganisationId(organisationId)
                .withEndDate(END_DATE)
                .withUserId(USER_ID)
                .build();

        return Envelope.envelopeFrom(metadataBuilder, defenceOrganisationDisassociated);
    }

    private MetadataBuilder getMetadataBuilder(final String eventName) {
        return JsonEnvelope.metadataBuilder()
                .withId(randomUUID())
                .withName(eventName)
                .withUserId(randomUUID().toString());
    }
}