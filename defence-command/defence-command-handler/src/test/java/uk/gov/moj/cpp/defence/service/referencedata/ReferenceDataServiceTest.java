package uk.gov.moj.cpp.defence.service.referencedata;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static uk.gov.justice.cps.defence.Offence.offence;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.moj.cpp.defence.test.utils.HandlerTestHelper.metadataFor;

import uk.gov.justice.cps.defence.Offence;
import uk.gov.justice.cps.defence.OffenceCodeReferenceData;
import uk.gov.justice.cps.defence.ReferenceDataOffencesListRequest;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.defence.common.util.GenericEnveloper;
import uk.gov.moj.cpp.referencedata.query.OffencesList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReferenceDataServiceTest {

    private ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

    @Mock
    private Requester requesterMock;

    @Spy
    private GenericEnveloper genericEnveloper = new GenericEnveloper();

    @InjectMocks
    private ReferenceDataService referenceDataService;

    @Test
    public void shouldGenerateListContainingReferenceDataValues() {

        doAnswer(invocation -> {
                    final Envelope<ReferenceDataOffencesListRequest> envelope = invocation.getArgument(0, Envelope.class);
                    final ReferenceDataOffencesListRequest request = envelope.payload();
                    return refDataOffenceDataForCjsOffencecode(request.getCjsoffencecode());
                }
        ).when(requesterMock).request(any(Envelope.class), eq(OffencesList.class));

        final List<OffenceCodeReferenceData> offenceCodeReferenceDataList =
                referenceDataService.retrieveReferenceDataForOffences(createTestOffenceList(), metadataFor("command", randomUUID()), false);

        assertThat(offenceCodeReferenceDataList, is(notNullValue()));
        assertThat(offenceCodeReferenceDataList.size(), is(4));

        offenceCodeReferenceDataList.forEach(refData -> {
            final String cjsCode = refData.getCjsoffencecode();
            assertThat(cjsCode, is(notNullValue()));
            assertThat(refData.getTitle(), is(notNullValue()));
            assertThat(refData.getLegislation(), is(notNullValue()));
            assertThat(substringToColon(refData.getTitle()), is("REFDATA TITLE CODE " + cjsCode));
            assertThat(substringToColon(refData.getLegislation()), is("REFDATA LEGISLATION CODE " + cjsCode));
        });
    }

    @Test
    public void shouldGenerateListContainingReferenceDataValuesForSowRef() {

        final AtomicReference<ReferenceDataOffencesListRequest> request = new AtomicReference<>();

        doAnswer(invocation -> {
                    final Envelope<ReferenceDataOffencesListRequest> envelope = invocation.getArgument(0, Envelope.class);
                    request.set(envelope.payload());
                    return refDataOffenceDataForCjsOffencecode(request.get().getCjsoffencecode());
                }
        ).when(requesterMock).request(any(Envelope.class), eq(OffencesList.class));

        final List<OffenceCodeReferenceData> offenceCodeReferenceDataList =
                referenceDataService.retrieveReferenceDataForOffences(createTestOffenceList(), metadataFor("command", randomUUID()), true);

        assertThat(offenceCodeReferenceDataList, is(notNullValue()));
        assertThat(offenceCodeReferenceDataList.size(), is(4));

        offenceCodeReferenceDataList.forEach(refData -> {
            final String cjsCode = refData.getCjsoffencecode();
            assertThat(cjsCode, is(notNullValue()));
            assertThat(refData.getTitle(), is(notNullValue()));
            assertThat(refData.getLegislation(), is(notNullValue()));
            assertThat(substringToColon(refData.getTitle()), is("REFDATA TITLE CODE " + cjsCode));
            assertThat(substringToColon(refData.getLegislation()), is("REFDATA LEGISLATION CODE " + cjsCode));
            assertThat(request.get().getSowRef(), is("moj"));
        });
    }



    private String substringToColon(String str) {
        final int colonIndex = str.indexOf(":");
        return str.substring(0, colonIndex);
    }

    private List<Offence> createTestOffenceList() {
        return asList(
                offence().withCjsCode("OF61131").withStartDate("2018-01-01").build(),
                offence().withCjsCode("PS90010").withStartDate("2018-01-01").build(),
                offence().withCjsCode("AB00001").withStartDate("2018-01-01").build(),
                offence().withCjsCode("YZ99999").withStartDate("2018-01-01").build());
    }

    private Envelope<OffencesList> refDataOffenceDataForCjsOffencecode(final String cjsCode) throws IOException {
        return responseFromJsonFile(format("testdata/referencedata.query.%s.json", cjsCode));
    }

    private Envelope<OffencesList> responseFromJsonFile(final String filepath) throws IOException {
        final String refdataPayLoad = loadJsonData(filepath);
        final OffencesList offencesList = mapper.readValue(refdataPayLoad, OffencesList.class);
        return envelopeFrom(metadataFor("command", randomUUID()), offencesList);
    }

    private static String loadJsonData(final String path) {
        String request = null;
        try {
            request = Resources.toString(
                    Resources.getResource(path),
                    Charset.defaultCharset()
            );
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error consuming file from location " + path);
        }
        return request;
    }

}
