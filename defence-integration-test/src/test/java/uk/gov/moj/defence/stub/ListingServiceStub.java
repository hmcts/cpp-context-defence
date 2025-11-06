package uk.gov.moj.defence.stub;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.moj.defence.stub.StubUtil.resourceToString;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

public class ListingServiceStub {

    private static final String LISTING_QUERY_URL = "/listing-service/query/api/rest/listing/hearings/allocated-and-unallocated.*";

    private static final String LISTING_MEDIA_TYPE = "application/vnd.listing.search.hearings+json";

    public static void stubListingService() {
        stubFor(get(urlPathMatching(LISTING_QUERY_URL))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", LISTING_MEDIA_TYPE)
                        .withBody(resourceToString("stub-data/listing-service/listing.query.searching.hearings.json")
                                .replace("START_DATE", LocalDate.now().plusDays(2).toString())
                                .replace("START_TIME", ZonedDateTime.now().plusDays(2).toString())
                        )

                ));
    }


    public static void stubListingServiceForCasesByPersonDefendantAndHearingDate(final String urn, final String resource) {
        final String url="/listing-service/query/api/rest/listing/cases/by-person-defendant-and-hearingDate?.*";

        stubFor(get(urlMatching(url))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", "application/vnd.listing.get.cases-by-person-defendant+json")
                        .withBody(resourceToString(resource)
                                .replaceAll("CASE_URN", urn))));
    }

    public static void stubListingServiceForCasesByOrganisationDefendantAndHearingDate(final String urn, final String resource) {
        final String url="/listing-service/query/api/rest/listing/cases/by-organisation-defendant-and-hearingDate?.*";

        stubFor(get(urlMatching(url))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", UUID.randomUUID().toString())
                        .withHeader("Content-Type", "application/vnd.listing.get.cases-by-organisation-defendant+json")
                        .withBody(resourceToString(resource)
                                .replaceAll("CASE_URN", urn))));
    }

}
