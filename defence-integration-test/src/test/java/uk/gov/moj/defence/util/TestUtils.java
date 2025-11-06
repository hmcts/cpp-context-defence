package uk.gov.moj.defence.util;


import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClient.TIMEOUT_IN_MILLIS;
import static uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClientBuilder.aMessageConsumerClient;
import static uk.gov.moj.defence.helper.PostgresDataSourceProvider.getPostgresDataSource;
import static uk.gov.moj.defence.helper.TopicNames.DEFENCE_EVENT_TOPIC_NAME;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClient;
import uk.gov.justice.services.test.utils.core.messaging.Poller;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;
import uk.gov.moj.defence.helper.JMSTopicHelper;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.UUID;

import com.google.common.io.Resources;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {

    public static final MessageConsumerClient DEFENCE_CONSUMER = aMessageConsumerClient().build();
    private static final String SELECT_DEFENCE_CLIENT_BY_DEFENDANT_ID = "select * from defence_client where defendant_id = ?";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);


    public static String getPayloadForCreatingRequest(final String path) {
        String request = null;
        try {
            request = Resources.toString(
                    Resources.getResource(path),
                    Charset.defaultCharset()
            );
        } catch (final Exception e) {
            LOGGER.error("Error consuming file from location {}", path);
            fail("Error consuming file from location " + path);
        }
        return request;
    }

    public static void waitForDefenceClientToBeUpdated(final UUID defendantID) {

        final QueryRunner queryRunner = new QueryRunner(getPostgresDataSource());
        final Poller poller = new Poller(150, 5);
        final ResultSetHandler<DefenceClient> resultSetHandler = new BeanHandler<>(DefenceClient.class);

        poller.pollUntilFound(() -> {
            try {
                if (queryRunner.query(SELECT_DEFENCE_CLIENT_BY_DEFENDANT_ID, resultSetHandler, defendantID) != null) {
                    return of(true);
                }
            } catch (final SQLException e) {
                LOGGER.error("Error while updating defence client {}", e);
                fail("Error while updating defence client " + e);
            }
            return empty();
        });

    }

    public static void postMessageToTopicAndVerify(final String payload, final String eventNameToVerify, final String publicEventToIssue) {
        postMessageToTopicAndVerify(payload, eventNameToVerify, publicEventToIssue, true);
    }

    public static void postMessageToTopicAndVerify(final String payload, final String eventNameToVerify, final String publicEventToIssue, final boolean verify) {
        final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

        try (final JMSTopicHelper publicTopicHelper = new JMSTopicHelper();
             final MessageConsumerClient defenceConsumer = aMessageConsumerClient().build()) {
            defenceConsumer.startConsumer(eventNameToVerify, DEFENCE_EVENT_TOPIC_NAME);
            publicTopicHelper.startProducer("public.event");
            publicTopicHelper.sendMessage(publicEventToIssue, stringToJsonObjectConverter.convert(payload));
            if (verify) {
                assertThat(eventNameToVerify + " message not found in defence.event topic", defenceConsumer.retrieveMessage(TIMEOUT_IN_MILLIS).isPresent(), is(true));
            }
        }
    }

    public static void postMessageToTopic(final String payload, final String publicEventToIssue) {
        final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

        try (final JMSTopicHelper publicTopicHelper = new JMSTopicHelper()) {
            publicTopicHelper.startProducer("public.event");
            publicTopicHelper.sendMessage(publicEventToIssue, stringToJsonObjectConverter.convert(payload));
        }
    }
}
