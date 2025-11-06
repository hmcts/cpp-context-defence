package uk.gov.moj.cpp.defence.event.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.cps.defence.OffencePleaDetails;
import uk.gov.justice.cps.defence.PleasAllocationDetails;
import uk.gov.justice.cps.defence.YesNoNa;
import uk.gov.justice.cps.defence.plea.PleaDefendantDetails;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.defence.common.util.GenericEnveloper;
import uk.gov.moj.cpp.defence.events.AllocationPleasAdded;
import uk.gov.moj.cpp.defence.events.AllocationPleasUpdated;
import uk.gov.moj.cpp.defence.persistence.DefendantAllocationRepository;
import uk.gov.moj.cpp.defence.persistence.entity.DefendantAllocation;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefencePleaEventListenerTest {

    @InjectMocks
    private DefencePleaEventsListener defencePleaEventsListener;

    @Mock
    private DefendantAllocationRepository allocationRepository;

    @Captor
    private ArgumentCaptor<DefendantAllocation> argumentCaptor;

    private final GenericEnveloper genericEnveloper = new GenericEnveloper();

    @ParameterizedTest
    @CsvSource({
                "false,false",
                "true, false",
                "false, true"
    })
    public void shouldSavePleas(Boolean first, Boolean second) {
        final Envelope<AllocationPleasAdded> event = createEnvelopeForOffencePleas(first, second);
        defencePleaEventsListener.saveAllocationPlea(event);
        verify(allocationRepository, times(1)).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getClass().getName(), is(DefendantAllocation.class.getName()));
        assertThat(argumentCaptor.getValue().getAcknowledgement(), is(true));
    }

    @Test
    public void shouldUpdatePleasWithDefendantDetailsConfirmedFlagTrue() {
        final Envelope<AllocationPleasUpdated> event = updateEnvelopeForOffencePleas(true);
        when(allocationRepository.findBy(any())).thenReturn(new DefendantAllocation());
        defencePleaEventsListener.updatePlea(event);
        verify(allocationRepository, times(1)).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getClass().getName(), is(DefendantAllocation.class.getName()));
        assertThat(argumentCaptor.getValue().getAcknowledgement(), is(true));
        assertThat(argumentCaptor.getValue().getDefendantFirstName(), nullValue());
        assertThat(argumentCaptor.getValue().getDefendantMiddleName(), nullValue());
        assertThat(argumentCaptor.getValue().getDefendantSurname(), nullValue());
        assertThat(argumentCaptor.getValue().getDefendantDateOfBirth(), nullValue());
        assertThat(argumentCaptor.getValue().getDefendantOrganisationName(), nullValue());
    }

    @Test
    public void shouldUpdatePleasWithDefendantDetailsConfirmedFlagFalse() {
        final Envelope<AllocationPleasUpdated> event = updateEnvelopeForOffencePleas(false);
        final AllocationPleasUpdated allocationPleasUpdated = event.payload();
        when(allocationRepository.findBy(any())).thenReturn(new DefendantAllocation());
        defencePleaEventsListener.updatePlea(event);
        verify(allocationRepository, times(1)).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getClass().getName(), is(DefendantAllocation.class.getName()));
        assertThat(argumentCaptor.getValue().getAcknowledgement(), is(true));
        assertThat(argumentCaptor.getValue().getDefendantFirstName(), is(allocationPleasUpdated.getPleasAllocation().getDefendantDetails().getFirstName()));
        assertThat(argumentCaptor.getValue().getDefendantMiddleName(), is(allocationPleasUpdated.getPleasAllocation().getDefendantDetails().getMiddleName()));
        assertThat(argumentCaptor.getValue().getDefendantSurname(), is(allocationPleasUpdated.getPleasAllocation().getDefendantDetails().getSurname()));
        assertThat(argumentCaptor.getValue().getDefendantDateOfBirth(), is(allocationPleasUpdated.getPleasAllocation().getDefendantDetails().getDob()));
        assertThat(argumentCaptor.getValue().getDefendantOrganisationName(), is(allocationPleasUpdated.getPleasAllocation().getDefendantDetails().getOrganisationName()));
    }

    @Test
    public void shouldUpdatePleasWhenRepositoryReturnedNullObject() {
        final Envelope<AllocationPleasUpdated> event = updateEnvelopeForOffencePleas(true);
        assertThrows(NullPointerException.class, ()-> defencePleaEventsListener.updatePlea(event));
    }

    private Envelope<AllocationPleasAdded> createEnvelopeForOffencePleas(final boolean defendantDetailsConfirmed, final boolean orgDefendant) {
        final Metadata metadata = getMetaData();

        final List<OffencePleaDetails> offencePleas = new ArrayList<>();

        final PleasAllocationDetails pleasAllocation = new PleasAllocationDetails.Builder()
                .withAcknowledgement(true)
                .withAllocationId(randomUUID())
                .withConsentToMagistratesCourtTrial("")
                .withCrownCourtObjection(YesNoNa.Y)
                .withDefendantId(randomUUID())
                .withDisputeOffenceValue(true)
                .withDisputeOffenceValueDetails("")
                .withElectingCrownCourtTrial(true)
                .withElectingCrownCourtTrialDetails("")
                .withOffencePleas(offencePleas)
                .withSentencingIndication(YesNoNa.Y)
                .withDefendantNameDobConfirmation(defendantDetailsConfirmed)
                .withDefendantDetails(getDefendantDetails(defendantDetailsConfirmed, orgDefendant))
                .build();
        final AllocationPleasAdded accessGranted = AllocationPleasAdded.allocationPleasAdded()
                .withPleasAllocation(pleasAllocation)
                .build();

        return genericEnveloper.envelopeWithNewActionName(accessGranted, metadata, "defence.events.allocation-pleas-added");
    }


    private Envelope<AllocationPleasUpdated> updateEnvelopeForOffencePleas(final boolean defendantDetailsConfirmed) {
        final Metadata metadata = getMetaData();

        final List<OffencePleaDetails> offencePleas = new ArrayList<>();

        final PleasAllocationDetails pleasAllocation = new PleasAllocationDetails.Builder()
                .withAcknowledgement(true)
                .withAllocationId(randomUUID())
                .withConsentToMagistratesCourtTrial("")
                .withCrownCourtObjection(YesNoNa.Y)
                .withDefendantId(randomUUID())
                .withDisputeOffenceValue(true)
                .withDisputeOffenceValueDetails("")
                .withElectingCrownCourtTrial(true)
                .withElectingCrownCourtTrialDetails("")
                .withOffencePleas(offencePleas)
                .withSentencingIndication(YesNoNa.Y)
                .withDefendantNameDobConfirmation(defendantDetailsConfirmed)
                .withDefendantDetails(getDefendantDetails(defendantDetailsConfirmed, false))
                .build();
        final AllocationPleasUpdated accessGranted = AllocationPleasUpdated.allocationPleasUpdated()
                .withPleasAllocation(pleasAllocation)
                .build();

        return genericEnveloper.envelopeWithNewActionName(accessGranted, metadata, "defence.events.allocation-pleas-added");
    }

    private PleaDefendantDetails getDefendantDetails(final boolean defendantDetailsConfirmed, final boolean orgDefendant) {
        if (defendantDetailsConfirmed) {
            return null;
        }

        PleaDefendantDetails.Builder builder = PleaDefendantDetails.pleaDefendantDetails();
        if(orgDefendant){
            builder.withOrganisationName("Org Name");
        } else {
            builder.withFirstName("firstName").withSurname("surname").withDob(LocalDate.now());
        }

        return builder.build();
    }

    public Metadata getMetaData() {
        return Envelope.metadataBuilder()
                .withId(randomUUID())
                .withName("defence.event.allocation-pleas-added")
                .createdAt(ZonedDateTime.now())
                .build();
    }

    private Envelope<AllocationPleasUpdated> updateEnvelopeForOffencePleas_NotGuilty(final boolean defendantDetailsConfirmed) {
        final Metadata metadata = getMetaData();

        final List<OffencePleaDetails> offencePleas = new ArrayList<>();
        offencePleas.add(OffencePleaDetails.offencePleaDetails()
                        .withIndicatedPlea("NotGuilty")
                        .withOffenceId(randomUUID())
                .build());

        final PleasAllocationDetails pleasAllocation = new PleasAllocationDetails.Builder()
                .withAcknowledgement(true)
                .withAllocationId(randomUUID())
                .withConsentToMagistratesCourtTrial("")
                .withDefendantId(randomUUID())
                .withDisputeOffenceValue(true)
                .withDisputeOffenceValueDetails("")
                .withElectingCrownCourtTrial(true)
                .withElectingCrownCourtTrialDetails("")
                .withOffencePleas(offencePleas)
                .withSentencingIndication(YesNoNa.Y)
                .withDefendantNameDobConfirmation(defendantDetailsConfirmed)
                .withDefendantDetails(getDefendantDetails(defendantDetailsConfirmed, false))
                .build();
        final AllocationPleasUpdated accessGranted = AllocationPleasUpdated.allocationPleasUpdated()
                .withPleasAllocation(pleasAllocation)
                .build();

        return genericEnveloper.envelopeWithNewActionName(accessGranted, metadata, "defence.events.allocation-pleas-added");
    }

    @Test
    public void shouldUpdatePleasWithDefendantDetails() {
        final Envelope<AllocationPleasUpdated> event = updateEnvelopeForOffencePleas_NotGuilty(true);
        when(allocationRepository.findBy(any())).thenReturn(new DefendantAllocation());
        defencePleaEventsListener.updatePlea(event);
        verify(allocationRepository, times(1)).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getClass().getName(), is(DefendantAllocation.class.getName()));
        assertThat(argumentCaptor.getValue().getAcknowledgement(), is(true));
        assertThat(argumentCaptor.getValue().getDefendantFirstName(), nullValue());
        assertThat(argumentCaptor.getValue().getDefendantMiddleName(), nullValue());
        assertThat(argumentCaptor.getValue().getDefendantSurname(), nullValue());
        assertThat(argumentCaptor.getValue().getDefendantDateOfBirth(), nullValue());
        assertThat(argumentCaptor.getValue().getDefendantOrganisationName(), nullValue());
    }
}
