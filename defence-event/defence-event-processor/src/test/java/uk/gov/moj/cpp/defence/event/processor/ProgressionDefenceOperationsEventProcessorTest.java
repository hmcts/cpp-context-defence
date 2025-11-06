package uk.gov.moj.cpp.defence.event.processor;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.moj.cpp.defence.event.processor.events.RepresentationType.REPRESENTATION_ORDER_APPLIED_FOR;

import uk.gov.justice.core.courts.AssociatedDefenceOrganisation;
import uk.gov.justice.core.courts.FundingType;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.progression.courts.DefendantLegalaidStatusUpdated;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.defence.event.processor.events.CaseDefendantChanged;
import uk.gov.moj.cpp.defence.event.processor.events.DefenceOrganisationForLaaAssociated;
import uk.gov.moj.cpp.defence.event.processor.events.DefenceOrganisationForLaaDisassociated;
import uk.gov.moj.cpp.defence.event.processor.events.Defendant;
import uk.gov.moj.cpp.defence.event.processor.events.DefendantLaaContractAssociated;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProgressionDefenceOperationsEventProcessorTest {


    private static final String PUBLIC_PROGRESSION_CASE_DEFENDANT_CHANGED = "public.progression.case-defendant-changed";
    private static final String DEFENCE_COMMAND_CASE_DEFENDANT_CHANGED = "defence.command.case-defendant-changed";
    private static final String LAA_CONTRACT_NUMBER = "AL23467892";
    private static final UUID USER_ID = randomUUID();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID ORGANISATION_ID = randomUUID();
    private static final String ORGANISATION_NAME = "LLOYDS";
    private static final UUID CASE_ID = randomUUID();

    @Captor
    private ArgumentCaptor<Envelope<CaseDefendantChanged>> caseDefendantChangedArgumentCaptor;

    @Captor
    private ArgumentCaptor<Envelope<DefenceOrganisationForLaaAssociated>> associateDefenceOrganisationForLAAArgumentCaptor;

    @Captor
    private ArgumentCaptor<Envelope<DefenceOrganisationForLaaDisassociated>> defenceOrganisationForLaaDisassociatedCaptor;

    @Captor
    private ArgumentCaptor<Envelope<DefendantLaaContractAssociated>> lockDefenceAssociationForLaaArgumentCaptor;

    @Captor
    private ArgumentCaptor<Envelope<DefendantLegalaidStatusUpdated>> defendantLegalaidStatusUpdatedCaptor;

    @Mock
    private Sender sender;

    @InjectMocks
    ProgressionDefenceOperationsEventProcessor progressionDefenceOperationsEventProcessor;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();


    @Test
    public void handleCaseDefendantChanged() {
        //Given
        final Envelope<CaseDefendantChanged> event = buildCaseDefendantChangedEvent();

        //When
        progressionDefenceOperationsEventProcessor.handleCaseDefendantChanged(event);

        //Then
        verifyCommandForCaseDefendantChanged();
    }

    @Test
    public void handleAssociateDefenceOrganisationForLAA() {
        //Given
        final Envelope<DefenceOrganisationForLaaAssociated> event = buildAssociateDefenceOrganisationForLAA();

        //When
        progressionDefenceOperationsEventProcessor.handleAssociateDefenceOrganisationForLAA(event);

        //Then
        verifyAssociateDefenceOrganisationForLAA("defence.command.associate-defence-organisation-for-laa");
    }

    @Test
    public void handleDisassociateDefenceOrganisationForLAA() {
        //Given
        final Envelope<DefenceOrganisationForLaaDisassociated> event = buildDefenceOrganisationForLaaDisassociated();

        //When
        progressionDefenceOperationsEventProcessor.handleDefenceOrganisationForLAADisassociated(event);

        //Then
        verifyDisassociateDefenceOrganisationForLAA("defence.command.disassociate-defence-organisation");
    }

    @Test
    public void handleDefendantLaaContractAssociated() {
        //Given
        final Envelope<DefendantLaaContractAssociated> event = buildDefendantLaaContractAssociated();

        //When
        progressionDefenceOperationsEventProcessor.handleDefendantLaaContractAssociated(event);

        //Then
        verifyLockDefenceAssociationForLaa("defence.command.handler.lock-defence-association-for-laa");
    }


    @Test
    public void handleDefendantLegalStatusUpdated() {
        //Given
        final Envelope<DefendantLegalaidStatusUpdated> event = buildDefendantLegalaidStatusUpdated(CASE_ID,"Represented");

        //When
        progressionDefenceOperationsEventProcessor.handleDefendantLegalStatusUpdated(event);

        //Then
        verifyDefendantLegalstatusUpdate("defence.command.record-defendant-legal-status-updated");
    }

    protected Envelope<CaseDefendantChanged> buildCaseDefendantChangedEvent() {
        final MetadataBuilder metadataBuilder = getMetadataBuilder(PUBLIC_PROGRESSION_CASE_DEFENDANT_CHANGED);

        Person personDetails = Person.person().withFirstName("name1").withLastName("lastname1").withDateOfBirth("01/01/1982").build();
        PersonDefendant personDefendant = PersonDefendant.personDefendant().withPersonDetails(personDetails).build();
        AssociatedDefenceOrganisation associatedDefenceOrganisation = AssociatedDefenceOrganisation.associatedDefenceOrganisation().withFundingType(FundingType.COURT_APPOINTED).build();
        Defendant defendant = Defendant.defendant().withPersonDefendant(personDefendant).withAssociatedDefenceOrganisation(associatedDefenceOrganisation).build();
        CaseDefendantChanged caseDefendantChanged = CaseDefendantChanged.caseDefendantChanged()
                .withDefendant(defendant)
                .build();

        return Envelope.envelopeFrom(metadataBuilder, caseDefendantChanged);
    }

    protected Envelope<DefenceOrganisationForLaaAssociated> buildAssociateDefenceOrganisationForLAA() {
        final MetadataBuilder metadataBuilder = getMetadataBuilder("public.progression.defence-organisation-for-laa-associated");

        DefenceOrganisationForLaaAssociated associateDefenceOrganisationForLaa = DefenceOrganisationForLaaAssociated.defenceOrganisationForLaaAssociated()
                .withLaaContractNumber(LAA_CONTRACT_NUMBER)
                .withDefendantId(DEFENDANT_ID)
                .withOrganisationId(ORGANISATION_ID)
                .withOrganisationName(ORGANISATION_NAME)
                .withRepresentationType(REPRESENTATION_ORDER_APPLIED_FOR)
                .build();

        return Envelope.envelopeFrom(metadataBuilder, associateDefenceOrganisationForLaa);
    }
    protected Envelope<DefendantLegalaidStatusUpdated> buildDefendantLegalaidStatusUpdated(UUID caseId, String legalAidStatus) {
        final MetadataBuilder metadataBuilder = getMetadataBuilder("public.progression.defendant-legalaid-status-updated");

        DefendantLegalaidStatusUpdated defendantLegalaidStatusUpdatedEnvelope = DefendantLegalaidStatusUpdated.defendantLegalaidStatusUpdated()
                .withLaaContractNumber(LAA_CONTRACT_NUMBER)
                .withDefendantId(DEFENDANT_ID)
                .withLegalAidStatus(legalAidStatus)
                .withCaseId(caseId)
                .build();

        return Envelope.envelopeFrom(metadataBuilder, defendantLegalaidStatusUpdatedEnvelope);
    }

    protected Envelope<DefenceOrganisationForLaaDisassociated> buildDefenceOrganisationForLaaDisassociated() {
        final MetadataBuilder metadataBuilder = getMetadataBuilder("public.progression.defence-organisation-for-laa-disassociated");

        DefenceOrganisationForLaaDisassociated defenceOrganisationForLaaDisassociated = DefenceOrganisationForLaaDisassociated
                .defenceOrganisationForLaaDisassociated()
                .withCaseId(CASE_ID)
                .withDefendantId(DEFENDANT_ID)
                .withOrganisationId(ORGANISATION_ID)
                .build();

        return Envelope.envelopeFrom(metadataBuilder, defenceOrganisationForLaaDisassociated);
    }

    protected Envelope<DefendantLaaContractAssociated> buildDefendantLaaContractAssociated() {
        final MetadataBuilder metadataBuilder = getMetadataBuilder("public.progression.defendant-laa-contract-associated");

        DefendantLaaContractAssociated defendantLaaContractAssociated = DefendantLaaContractAssociated.defendantLaaContractAssociated()
                .withDefendantId(DEFENDANT_ID)
                .withLaaContractNumber(LAA_CONTRACT_NUMBER)
                .build();

        return Envelope.envelopeFrom(metadataBuilder, defendantLaaContractAssociated);
    }


    private MetadataBuilder getMetadataBuilder(final String eventName) {
        return JsonEnvelope.metadataBuilder()
                .withId(randomUUID())
                .withName(eventName)
                .withUserId(USER_ID.toString());
    }

    private void verifyAssociateDefenceOrganisationForLAA(final String commandName) {
        verify(sender).send(associateDefenceOrganisationForLAAArgumentCaptor.capture());
        assertThat(commandName, is(associateDefenceOrganisationForLAAArgumentCaptor.getValue().metadata().name()));
        final DefenceOrganisationForLaaAssociated capturePayload = associateDefenceOrganisationForLAAArgumentCaptor.getValue().payload();
        assertThat(DEFENDANT_ID, is(capturePayload.getDefendantId()));
        assertThat(ORGANISATION_ID, is(capturePayload.getOrganisationId()));
        assertThat(LAA_CONTRACT_NUMBER, is(capturePayload.getLaaContractNumber()));
        assertThat(REPRESENTATION_ORDER_APPLIED_FOR, is(capturePayload.getRepresentationType()));
        assertThat(ORGANISATION_NAME, is(capturePayload.getOrganisationName()));
    }


    private void verifyDefendantLegalstatusUpdate(final String commandName) {
        verify(sender).send(defendantLegalaidStatusUpdatedCaptor.capture());
        assertThat(commandName, is(defendantLegalaidStatusUpdatedCaptor.getValue().metadata().name()));
        final DefendantLegalaidStatusUpdated capturePayload = defendantLegalaidStatusUpdatedCaptor.getValue().payload();
        assertThat(DEFENDANT_ID, is(capturePayload.getDefendantId()));
        assertThat(LAA_CONTRACT_NUMBER, is(capturePayload.getLaaContractNumber()));
    }

    private void verifyDisassociateDefenceOrganisationForLAA(final String commandName) {
        verify(sender).send(defenceOrganisationForLaaDisassociatedCaptor.capture());
        assertThat(commandName, is(defenceOrganisationForLaaDisassociatedCaptor.getValue().metadata().name()));
        final DefenceOrganisationForLaaDisassociated capturePayload = defenceOrganisationForLaaDisassociatedCaptor.getValue().payload();
        assertThat(DEFENDANT_ID, is(capturePayload.getDefendantId()));
        assertThat(ORGANISATION_ID, is(capturePayload.getOrganisationId()));
        assertThat(CASE_ID, is(capturePayload.getCaseId()));
    }

    private void verifyLockDefenceAssociationForLaa(final String commandName) {
        verify(sender).send(lockDefenceAssociationForLaaArgumentCaptor.capture());
        assertThat(commandName, is(lockDefenceAssociationForLaaArgumentCaptor.getValue().metadata().name()));
        final DefendantLaaContractAssociated capturePayload = lockDefenceAssociationForLaaArgumentCaptor.getValue().payload();
        assertThat(DEFENDANT_ID, is(capturePayload.getDefendantId()));
    }

    private void verifyCommandForCaseDefendantChanged() {
        verify(sender).send(caseDefendantChangedArgumentCaptor.capture());
        assertThat(DEFENCE_COMMAND_CASE_DEFENDANT_CHANGED, is(caseDefendantChangedArgumentCaptor.getValue().metadata().name()));
    }


}