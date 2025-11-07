package uk.gov.moj.cpp.defence.aggregate;

import static java.util.stream.Stream.of;
import static uk.gov.justice.cps.defence.DefendantDetails.defendantDetails;
import static uk.gov.justice.cps.defence.DuplicateDefendantReceivedAgainstADefenceClient.duplicateDefendantReceivedAgainstADefenceClient;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.cps.defence.DefenceClientDetails;
import uk.gov.justice.cps.defence.DefenceClientMappedToACase;
import uk.gov.justice.cps.defence.DefendantAdded;
import uk.gov.justice.cps.defence.DefendantDetails;
import uk.gov.justice.cps.defence.DuplicateDefendantReceivedAgainstADefenceClient;
import uk.gov.justice.cps.defence.Offence;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.defence.events.ProsecutionCaseReceived;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class CaseDefenceClientMap implements Aggregate {

    private final Map<UUID, UUID> defenceClients = new HashMap<>();
    private final Set<UUID> defendants = new HashSet();
    private UUID caseId;
    private String urn;
    private Boolean isCivil;

    public Stream<Object> receiveDetails(final UUID caseId, final String urn, final String prosecutingAuthority, final Boolean isCivil, final Boolean isGroupMember) {

        final Stream.Builder<Object> streamBuilder = Stream.builder();

        streamBuilder.add(new ProsecutionCaseReceived(caseId, isCivil, isGroupMember, prosecutingAuthority, urn));

        return apply(streamBuilder.build());
    }

    public Stream<Object> addADefendant(final UUID defendantId,
                                        final DefendantDetails defendantDetails,
                                        final String policeDefendantId,
                                        final List<Offence> offences) {

        if (defendants.contains(defendantId)) {
            //Duplicate defendant id.ignore
            return apply(
                    of(duplicateDefendantReceivedAgainstADefenceClient()
                            .withDefendantDetails(defendantDetails()
                                    .withValuesFrom(defendantDetails)
                                    .withIsCivil(isCivil)
                                    .build())
                            .withPoliceDefendantId(policeDefendantId)
                            .withDuplicateDefendantId(defendantId)
                            .build()));
        }

        final Stream.Builder<Object> streamBuilder = Stream.builder();

        UUID defenceClientId = defenceClients.get(defendantDetails.getId());

        if (defenceClientId == null) {
            defenceClientId = defendantId;
            final DefenceClientMappedToACase defenceClientMappedToACase = DefenceClientMappedToACase.defenceClientMappedToACase()
                    .withDefenceClientId(defenceClientId)
                    .withDefendantDetails(defendantDetails()
                            .withValuesFrom(defendantDetails)
                            .withIsCivil(isCivil)
                            .build())
                    .withDefenceClientDetails(DefenceClientDetails.defenceClientDetails().build())
                    .withUrn(urn)
                    .withDefendantId(defendantId)
                    .build();

            streamBuilder.add(defenceClientMappedToACase);

        }

        streamBuilder.add(DefendantAdded.defendantAdded()
                .withDefenceClientId(defenceClientId)
                .withDefendantDetails(defendantDetails()
                        .withValuesFrom(defendantDetails)
                        .withIsCivil(isCivil)
                        .build())
                .withDefendantId(defendantId)
                .withOffences(offences)
                .withPoliceDefendantId(policeDefendantId)
                .build());

        return apply(streamBuilder.build());
    }

    public UUID getDefenceClientId(final UUID defendantId){
        return defenceClients.get(defendantId);
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(DefenceClientMappedToACase.class)
                        .apply(x -> {
                            this.caseId = x.getDefendantDetails().getCaseId();
                            this.defenceClients.put(x.getDefendantDetails().getId(), x.getDefenceClientId());
                        }),
                when(DefendantAdded.class)
                        .apply(defendantAdded -> this.defendants.add(defendantAdded.getDefendantId())
                        ),
                when(DuplicateDefendantReceivedAgainstADefenceClient.class)
                        .apply(duplicateDefendantReceivedAgainstADefenceClient -> {
                            //do nothing
                        }),
                when(ProsecutionCaseReceived.class)
                        .apply(x -> {
                            this.caseId = x.getCaseId();
                            this.urn = x.getUrn();
                            this.isCivil = x.getIsCivil();
                        }),
                otherwiseDoNothing());
    }

    public UUID getCaseId() {
        return caseId;
    }
    public Boolean getIsCivil() {
        return isCivil;
    }
}
