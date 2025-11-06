package uk.gov.moj.defence.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.moj.defence.stub.StubUtil.resourceToString;

import java.util.UUID;

public class ProgressionServiceStub {

    private static final String PROGRESSION_QUERY_PROSECUTION_CASE = "/progression-service/query/api/rest/progression/prosecutioncases/";
    private static final String PROGRESSION_QUERY_PROSECUTION_CASE_MEDIA_TYPE = "application/vnd.progression.query.prosecutioncase+json";


    public static void stubProgressionService(UUID caseId) {
        stubFor(get(urlPathMatching(PROGRESSION_QUERY_PROSECUTION_CASE + caseId.toString()))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", PROGRESSION_QUERY_PROSECUTION_CASE_MEDIA_TYPE)
                        .withBody(resourceToString("stub-data/progression-service/progression.query.prosecutioncase.json"))));
    }


    public static void stubProgressionService() {
        stubFor(get(urlPathMatching(PROGRESSION_QUERY_PROSECUTION_CASE + ".*"))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", PROGRESSION_QUERY_PROSECUTION_CASE_MEDIA_TYPE)
                        .withBody(resourceToString("stub-data/progression-service/progression.query.prosecutioncase.json"))));
    }


}
