package uk.gov.moj.defence.util;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;

public class WiremockHelper {

    public static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");

    public static void resetWiremock() {
        configureFor(HOST, 8080);
        reset();
    }
}
