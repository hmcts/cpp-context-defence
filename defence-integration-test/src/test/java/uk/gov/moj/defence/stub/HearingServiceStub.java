package uk.gov.moj.defence.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.moj.defence.stub.StubUtil.resourceToString;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

public class HearingServiceStub {

    private static final String HEARING_TIMLINE_QUERY_URL = "/hearing-service/query/api/rest/hearing/timeline/";
    private static final String HEARING_MEDIA_TYPE = "application/vnd.hearing.case.timeline+json";

    public static void stubHearingServiceForCaseTimeline(UUID caseId) {
        stubFor(get(urlPathMatching(HEARING_TIMLINE_QUERY_URL + caseId))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", HEARING_MEDIA_TYPE)
                        .withBody(resourceToString("stub-data/hearing-service/hearing-timeline.json")
                                .replace("START_DATE", LocalDate.now().plusDays(2).toString())
                                .replace("START_TIME", ZonedDateTime.now().plusDays(2).toString())
                        )

                ));
    }

    public static void stubHearingServiceForApplicationTimeline(UUID applicationId) {
        stubFor(get(urlPathMatching(HEARING_TIMLINE_QUERY_URL + applicationId))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", "application/vnd.hearing.application.timeline+json")
                        .withBody(resourceToString("stub-data/hearing-service/hearing-application-timeline.json"))));
    }
}
