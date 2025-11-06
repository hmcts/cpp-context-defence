package uk.gov.moj.defence.helper;

import static java.lang.String.format;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClient.TIMEOUT_IN_MILLIS;
import static uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClientBuilder.aMessageConsumerClient;
import static uk.gov.moj.defence.helper.TopicNames.PUBLIC_EVENT;
import static uk.gov.moj.defence.util.HttpHeaders.createHttpHeaders;

import uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClient;
import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.util.UUID;

public class DefenceGrantAccessHelper {


    public void postGrantDefenceAccessAndVerifyPublicEventEvent(final UUID defenceClientId, final String granteeEmail, final String granterUserId, final String expectedEventName) {
        try (final MessageConsumerClient messageConsumerClient = aMessageConsumerClient().build()) {

            messageConsumerClient.startConsumer(expectedEventName, PUBLIC_EVENT);

            final String queryString = format("/defence-command-api/command/api/rest/defence/defenceclients/%s/grantaccess", defenceClientId);
            final String payload = createObjectBuilder()
                    .add("granteeEmailId", granteeEmail)
                    .add("defenceClientId", defenceClientId.toString())
                    .build()
                    .toString();

            final RestClient restClient = new RestClient();

            restClient.postCommand(getBaseUri() + queryString,
                    "application/vnd.defence.grant-defence-access+json",
                    payload,
                    createHttpHeaders(granterUserId)
            );

            assertThat(expectedEventName + " message is not found in defence.event topic", messageConsumerClient.retrieveMessage(TIMEOUT_IN_MILLIS).isPresent(), is(true));
        }
    }

    public void postRemoveGrantDefenceAccessAndVerifyPublicEventEvent(final UUID defenceClientId, final UUID granteeUserId, final UUID granterUserId, final String expectedEventName) {
        try (final MessageConsumerClient messageConsumerClient = aMessageConsumerClient().build()) {

            messageConsumerClient.startConsumer(expectedEventName, PUBLIC_EVENT);


            final String queryString = format("/defence-command-api/command/api/rest/defence/defenceclients/%s/grantaccess", defenceClientId);
            final String payload = createObjectBuilder()
                    .add("granteeUserId", granteeUserId.toString())
                    .add("defenceClientId", defenceClientId.toString())
                    .build()
                    .toString();

            final RestClient restClient = new RestClient();

            restClient.postCommand(getBaseUri() + queryString,
                    "application/vnd.defence.remove-grant-defence-access+json",
                    payload,
                    createHttpHeaders(granterUserId.toString())
            );

            assertThat(expectedEventName + " message is not found in defence.event topic", messageConsumerClient.retrieveMessage(TIMEOUT_IN_MILLIS).isPresent(), is(true));
        }
    }



}