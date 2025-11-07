package uk.gov.moj.cpp.defence.aggregate;

import static java.util.Objects.isNull;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.cps.defence.PleasAllocationDetails;
import uk.gov.justice.cps.defence.plea.PleaDefendantDetails;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.defence.events.AllocationPleasAdded;
import uk.gov.moj.cpp.defence.events.AllocationPleasUpdated;
import uk.gov.moj.cpp.defence.events.OpaTaskRequested;

import java.util.stream.Stream;

public class DefencePleaAggregate implements Aggregate {
    private static final long serialVersionUID = 102L;
    private static final String LEGAL_ADVISERS = "Legal Advisers";
    private static final String REVIEW_INDICATED_PLEA = "Review indicated plea";
    private String caseUrn;

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(AllocationPleasAdded.class).apply(e ->
                        caseUrn = e.getPleasAllocation().getCaseUrn()
                ),
                otherwiseDoNothing()
        );
    }


    public Stream<Object> createPlea(final PleasAllocationDetails pleasAllocationDetails) {
        final Stream.Builder<Object> streamBuilder = Stream.builder();
        streamBuilder.add(AllocationPleasAdded.allocationPleasAdded().withPleasAllocation(pleasAllocationDetails).build());

        streamBuilder.add(OpaTaskRequested.opaTaskRequested()
                .withOpaId(pleasAllocationDetails.getAllocationId())
                .withCaseUrn(pleasAllocationDetails.getCaseUrn())
                .withCourtCode(pleasAllocationDetails.getHearingCourtCode())
                .withNumberOfDays(1)
                .withRoles(LEGAL_ADVISERS)
                .withTaskName(REVIEW_INDICATED_PLEA)
                .build());

        return apply(streamBuilder.build());
    }

    public Stream<Object> updatePlea(final PleasAllocationDetails pleasAllocationDetails) {
        return apply(
                Stream.of(
                        AllocationPleasUpdated.allocationPleasUpdated()
                                .withPleasAllocation(PleasAllocationDetails.pleasAllocationDetails()
                                        .withValuesFrom(pleasAllocationDetails)
                                        .withCaseUrn(this.caseUrn)
                                        .withDefendantDetails(getDefendantDetails(pleasAllocationDetails))
                                        .build())
                                .build()));
    }

    private PleaDefendantDetails getDefendantDetails(final PleasAllocationDetails pleasAllocationDetails) {
        if (isNull(pleasAllocationDetails.getDefendantDetails())) {
            return null;
        }
        //Defendant details coming from case are already correct. Clean the manually set values.
        if (isTrue(pleasAllocationDetails.getDefendantNameDobConfirmation())) {
            return null;
        }
        return pleasAllocationDetails.getDefendantDetails();
    }

    public static boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}
