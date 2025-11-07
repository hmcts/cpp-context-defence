package uk.gov.moj.cpp.defence.aggregate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.cps.defence.PleasAllocationDetails;
import uk.gov.justice.cps.defence.plea.PleaDefendantDetails;
import uk.gov.moj.cpp.defence.events.AllocationPleasAdded;
import uk.gov.moj.cpp.defence.events.AllocationPleasUpdated;
import uk.gov.moj.cpp.defence.events.OpaTaskRequested;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;


@ExtendWith(MockitoExtension.class)
public class DefencePleaAggregateTest {

    private static final UUID ENVELOPE_ID = UUID.randomUUID();
    public static final String FIRST_NAME_VALUE = "firstName";
    public static final String SURNAME_VALUE = "surname";
    final ZonedDateTime dateCreate = ZonedDateTime.now();
    @InjectMocks
    private DefencePleaAggregate defencePleaAggregate;

    @Test
    public void shouldCreatePleas() {
        final UUID defendantId = randomUUID();
        final String caseUrn = "CASEURN";

        final PleasAllocationDetails pleasAllocationDetails = PleasAllocationDetails.pleasAllocationDetails()
                .withDefendantId(defendantId)
                .withCaseUrn(caseUrn)
                .build();
        final Stream<Object> eventStream = defencePleaAggregate.createPlea(pleasAllocationDetails);

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(2));
        assertThat(eventList.get(0).getClass().getName(), is(AllocationPleasAdded.class.getName()));

        final AllocationPleasAdded offencePleasAdded = (AllocationPleasAdded) eventList.get(0);

        assertThat(offencePleasAdded.getPleasAllocation().getDefendantId(), is(defendantId));

        assertThat(eventList.get(0).getClass().getName(), is(AllocationPleasAdded.class.getName()));

        final OpaTaskRequested opaTaskRequested = (OpaTaskRequested) eventList.get(1);

        assertThat(opaTaskRequested.getCaseUrn(), is(caseUrn));
    }

    @Test
    public void shouldUpdatePleasWithNewDefendantInfo() {
        final UUID defendantId = randomUUID();
        final String urn = "urn1";

        final AllocationPleasAdded allocationPleasAdded = AllocationPleasAdded.allocationPleasAdded().withPleasAllocation(PleasAllocationDetails.pleasAllocationDetails().withCaseUrn(urn).build()).build();
        defencePleaAggregate.apply(allocationPleasAdded);

        final PleasAllocationDetails pleasAllocationDetails = PleasAllocationDetails.pleasAllocationDetails()
                .withDefendantId(defendantId)
                .withDefendantNameDobConfirmation(FALSE)
                .withDefendantDetails(PleaDefendantDetails.pleaDefendantDetails()
                        .withFirstName(FIRST_NAME_VALUE)
                        .withSurname(SURNAME_VALUE)
                        .withDob(LocalDate.now())
                        .build())
                .build();
        final Stream<Object> eventStream = defencePleaAggregate.updatePlea(pleasAllocationDetails);

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(AllocationPleasUpdated.class.getName()));

        final AllocationPleasUpdated offencePleasUpdated = (AllocationPleasUpdated) eventList.get(0);

        assertThat(offencePleasUpdated.getPleasAllocation().getDefendantId(), is(defendantId));
        assertThat(offencePleasUpdated.getPleasAllocation().getCaseUrn(), is(urn));
        assertThat(offencePleasUpdated.getPleasAllocation().getDefendantDetails().getFirstName(), is(pleasAllocationDetails.getDefendantDetails().getFirstName()));
        assertThat(offencePleasUpdated.getPleasAllocation().getDefendantDetails().getSurname(), is(pleasAllocationDetails.getDefendantDetails().getSurname()));
        assertThat(offencePleasUpdated.getPleasAllocation().getDefendantDetails().getDob(), is(pleasAllocationDetails.getDefendantDetails().getDob()));
    }

    @Test
    public void shouldUpdatePleasWithConfirmedDefendantInfo() {
        final UUID defendantId = randomUUID();
        final String urn = "urn1";

        final AllocationPleasAdded allocationPleasAdded = AllocationPleasAdded.allocationPleasAdded().withPleasAllocation(PleasAllocationDetails.pleasAllocationDetails().withCaseUrn(urn).build()).build();
        defencePleaAggregate.apply(allocationPleasAdded);

        final PleasAllocationDetails pleasAllocationDetails = PleasAllocationDetails.pleasAllocationDetails()
                .withDefendantId(defendantId)
                .withDefendantNameDobConfirmation(TRUE)
                .withDefendantDetails(PleaDefendantDetails.pleaDefendantDetails()
                        .withFirstName(FIRST_NAME_VALUE)
                        .withSurname(SURNAME_VALUE)
                        .withDob(LocalDate.now())
                        .build())
                .build();
        final Stream<Object> eventStream = defencePleaAggregate.updatePlea(pleasAllocationDetails);

        final List<?> eventList = eventStream.collect(Collectors.toList());
        assertThat(eventList.size(), is(1));
        assertThat(eventList.get(0).getClass().getName(), is(AllocationPleasUpdated.class.getName()));

        final AllocationPleasUpdated offencePleasUpdated = (AllocationPleasUpdated) eventList.get(0);

        assertThat(offencePleasUpdated.getPleasAllocation().getDefendantId(), is(defendantId));
        assertThat(offencePleasUpdated.getPleasAllocation().getCaseUrn(), is(urn));
        assertThat(offencePleasUpdated.getPleasAllocation().getDefendantDetails(), nullValue());
    }

}
