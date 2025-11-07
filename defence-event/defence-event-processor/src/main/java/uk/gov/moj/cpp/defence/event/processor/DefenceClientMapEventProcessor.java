package uk.gov.moj.cpp.defence.event.processor;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.messaging.Envelope.metadataFrom;

import uk.gov.justice.core.courts.DefendantsAddedToCase;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.defence.event.converter.DefendantsAddedConverter;
import uk.gov.moj.cpp.defence.event.converter.ProsecutionCaseConverter;
import uk.gov.moj.cpp.defence.event.processor.events.CaseRemovedFromGroupCases;
import uk.gov.moj.cpp.progression.json.schema.event.ProsecutionCaseCreated;

import javax.inject.Inject;

@ServiceComponent(EVENT_PROCESSOR)
public class DefenceClientMapEventProcessor {

    @Inject
    ProsecutionCaseConverter prosecutionCaseConverter;

    @Inject
    DefendantsAddedConverter defendantsAddedConverter;
    @Inject
    private Sender sender;

    @Handles("public.progression.prosecution-case-created")
    public void handleProsecutionCaseReceived(final Envelope<ProsecutionCaseCreated> envelope) {
        sender.send(envelopeFrom(metadataFrom(envelope.metadata()).withName("defence.command.prosecution-case-receive-details"),
                prosecutionCaseConverter.convertToProsecutionCaseReceiveDetails(envelope.payload().getProsecutionCase())));
    }

    @Handles("public.progression.case-removed-from-group-cases")
    public void handleCaseRemovedFromGroupCases(final Envelope<CaseRemovedFromGroupCases> envelope) {
        sender.send(envelopeFrom(metadataFrom(envelope.metadata()).withName("defence.command.prosecution-case-receive-details"),
                prosecutionCaseConverter.convertToProsecutionCaseReceiveDetails(envelope.payload().getRemovedCase())));
    }

    @Handles("public.progression.defendants-added-to-case")
    public void handleSpiProsecutionDefendantsAdded(final Envelope<DefendantsAddedToCase> envelope) {

        envelope.payload().getDefendants().forEach(defendant -> {
            defendantsAddedConverter.convert(defendant);
            sender.send(envelopeFrom(metadataFrom(envelope.metadata()).withName("defence.command.add-defendant"), defendantsAddedConverter.convert(defendant)));

        });

    }

}
