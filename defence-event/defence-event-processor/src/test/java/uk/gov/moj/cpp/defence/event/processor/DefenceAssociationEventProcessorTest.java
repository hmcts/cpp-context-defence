package uk.gov.moj.cpp.defence.event.processor;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.defence.common.util.DefencePermission.VIEW_DEFENDANT_PERMISSION;
import static uk.gov.moj.cpp.defence.events.RepresentationType.REPRESENTATION_ORDER_APPLIED_FOR;

import uk.gov.justice.cps.defence.Permission;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.defence.event.processor.commands.RemoveAllGrantDefenceAccess;
import uk.gov.moj.cpp.defence.event.service.DefenceService;
import uk.gov.moj.cpp.defence.events.DefenceAssociationFailed;
import uk.gov.moj.cpp.defence.events.DefenceDisassociationFailed;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationAssociated;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationDisassociated;
import uk.gov.moj.cpp.defence.events.Status;
import uk.gov.moj.cpp.defence.service.UserGroupService;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefenceAssociationEventProcessorTest {

    private static final String DEFENCE_ORGANISATION_ASSOCIATED_EVENT = "defence.event.defence-association-received";
    private static final String DEFENCE_ORGANISATION_DISASSOCIATED_EVENT = "defence.event.defence-disassociation-received";
    private static final String DEFENCE_ORGANISATION_ASSOCIATED_FAIL_EVENT = "defence.defence-organisation-association-failed";
    private static final String DEFENCE_ORGANISATION_DISASSOCIATED_FAIL_EVENT = "defence.event.defence-disassociation-failed";
    private static final String LAA_CONTRACT_NUMBER = "AL23467892";
    private static final UUID USER_ID = randomUUID();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final String ORGANISATION_NAME = "LLOYDS";
    private static final ZonedDateTime START_DATE = ZonedDateTime.now().minusDays(2);
    private static final ZonedDateTime END_DATE = ZonedDateTime.now();
    private static final UUID ORGANISATION_ID = randomUUID();


    @Mock
    private Sender sender;

    @Captor
    ArgumentCaptor<Envelope<?>> envelopeArgumentCaptor;

    @Mock
    private DefenceService defenceService;


    @InjectMocks
    private DefenceAssociationEventProcessor defenceAssociationEventProcessor;

    @Spy
    private UserGroupService usersGroupService;

    @Test
    public void handleOrganisationAssociated() {

        defenceAssociationEventProcessor.handleOrganisationAssociated(buildAssociatedEvent(ORGANISATION_ID));

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

        final Envelope<DefenceOrganisationAssociated> envelopeArgumentCaptorValue = (Envelope<DefenceOrganisationAssociated>) envelopeArgumentCaptor.getValue();

        assertThat(envelopeArgumentCaptorValue.metadata().name(), is("public.defence.defence-organisation-associated"));

    }

    @Test
    public void handleFailedOrganisationAssociated() {

        defenceAssociationEventProcessor.handleFailedOrganisationAssociated(buildFailedAssociatedEvent(DEFENDANT_ID));

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

        final Envelope<DefenceAssociationFailed> envelopeArgumentCaptorValue = (Envelope<DefenceAssociationFailed>) envelopeArgumentCaptor.getValue();

        assertThat(envelopeArgumentCaptorValue.metadata().name(), is("public.defence.defence-organisation-association-failed"));

    }


    @Test
    public void handleFailedOrganisationDisassociated() {

        defenceAssociationEventProcessor.handleFailedOrganisationDisassociated(buildFailedDisassociatedEvent(DEFENDANT_ID));

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

        final Envelope<DefenceAssociationFailed> envelopeArgumentCaptorValue = (Envelope<DefenceAssociationFailed>) envelopeArgumentCaptor.getValue();

        assertThat(envelopeArgumentCaptorValue.metadata().name(), is("public.defence.defence-organisation-disassociation-failed"));

    }

    @Test
    public void handleOrganisationDisassociated() {

        when(defenceService.getCaseIdForDefenceClient(any(), any())).thenReturn(randomUUID());

        defenceAssociationEventProcessor.handleOrganisationDisassociated(buildDisassociatedEvent(ORGANISATION_ID));

        verify(sender, times(2)).send(envelopeArgumentCaptor.capture());

        envelopeArgumentCaptor.getAllValues().forEach(envelope -> {

            if(envelope.payload() instanceof DefenceOrganisationAssociated){

                assertThat(envelope.metadata().name(), is("public.defence.defence-organisation-disassociated"));

            }else if(envelope.payload() instanceof RemoveAllGrantDefenceAccess){

                assertThat(envelope.metadata().name(), is("defence.command.remove-all-grant-defence-access"));

            }
        });
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
                .withPermissions(Arrays.asList(preparePermission(randomUUID(), DEFENDANT_ID, ORGANISATION_ID, Status.ADDED)))
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
                .withPermissions(Arrays.asList(preparePermission(randomUUID(), DEFENDANT_ID, ORGANISATION_ID, Status.DELETED)))
                .build();

        return Envelope.envelopeFrom(metadataBuilder, defenceOrganisationDisassociated);
    }


    protected Envelope<DefenceAssociationFailed> buildFailedAssociatedEvent(final UUID defendantId) {
        final MetadataBuilder metadataBuilder = getMetadataBuilder(DEFENCE_ORGANISATION_ASSOCIATED_FAIL_EVENT);

        DefenceAssociationFailed defenceAssociationFailed = DefenceAssociationFailed.defenceAssociationFailed().withDefendantId(defendantId).withFailureReason("Failed").build();

        return Envelope.envelopeFrom(metadataBuilder, defenceAssociationFailed);
    }

    protected Envelope<DefenceDisassociationFailed> buildFailedDisassociatedEvent(final UUID defendantId) {
        final MetadataBuilder metadataBuilder = getMetadataBuilder(DEFENCE_ORGANISATION_DISASSOCIATED_FAIL_EVENT);

        DefenceDisassociationFailed defenceAssociationFailed = DefenceDisassociationFailed.defenceDisassociationFailed().withDefendantId(defendantId).withFailureReason("Failed").build();

        return Envelope.envelopeFrom(metadataBuilder, defenceAssociationFailed);
    }

    private MetadataBuilder getMetadataBuilder(final String eventName) {
        return JsonEnvelope.metadataBuilder()
                .withId(randomUUID())
                .withName(eventName)
                .withUserId(randomUUID().toString());
    }

    private Permission preparePermission(final UUID permissionId, final UUID defendantId, final UUID organisationId, final Status status) {
        return Permission.permission()
                .withId(permissionId)
                .withTarget(defendantId)
                .withObject(VIEW_DEFENDANT_PERMISSION.getObjectType())
                .withAction(VIEW_DEFENDANT_PERMISSION.getActionType())
                .withSource(organisationId)
                .withStatus(status)
                .build();
    }
}
