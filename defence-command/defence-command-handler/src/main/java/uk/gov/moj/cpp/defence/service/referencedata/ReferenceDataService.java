package uk.gov.moj.cpp.defence.service.referencedata;

import uk.gov.justice.cps.defence.Offence;
import uk.gov.justice.cps.defence.OffenceCodeReferenceData;
import uk.gov.justice.cps.defence.ReferenceDataOffencesListRequest;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.defence.common.util.GenericEnveloper;
import uk.gov.moj.cpp.referencedata.query.English;
import uk.gov.moj.cpp.referencedata.query.Offences;
import uk.gov.moj.cpp.referencedata.query.OffencesList;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class ReferenceDataService {

    @ServiceComponent(Component.COMMAND_HANDLER)
    @Inject
    Requester requester;

    @Inject
    GenericEnveloper genericEnveloper;

    public List<OffenceCodeReferenceData> retrieveReferenceDataForOffences(final List<Offence> offenceList, final Metadata metadata) {

        return offenceList.stream()
                .map(offence -> {
                    final Offences refDataOffences = getRefDataOffences(offence.getCjsCode(),offence.getStartDate(),metadata);

                    final English english = refDataOffences
                            .getDetails()
                            .getDocument()
                            .getEnglish();

                    final OffenceCodeReferenceData.Builder offenceCodeReferenceDataBuilder = OffenceCodeReferenceData.offenceCodeReferenceData();
                    offenceCodeReferenceDataBuilder.withTitle(english.getTitle());
                    offenceCodeReferenceDataBuilder.withLegislation(english.getLegislation());
                    return offenceCodeReferenceDataBuilder.withCjsoffencecode(offence.getCjsCode())
                            .build();

                }).collect(Collectors.toList());
    }

    public Offences getRefDataOffences(final String cjsCode, final String startDate, final Metadata metadata){
        final ReferenceDataOffencesListRequest request = ReferenceDataOffencesListRequest.referenceDataOffencesListRequest()
                .withCjsoffencecode(cjsCode)
                .withDate(startDate)
                .build();

        final Envelope envelope = genericEnveloper.envelopeWithNewActionName(request, metadata, "referencedataoffences.query.offences-list");
        final Envelope<OffencesList> response = requester.request(envelope, OffencesList.class);
        final OffencesList refDataOffencesList = response.payload();

        // cjsoffencecode provided in query so there will only be one Offence in the response
        return refDataOffencesList.getOffences().get(0);
    }
}
