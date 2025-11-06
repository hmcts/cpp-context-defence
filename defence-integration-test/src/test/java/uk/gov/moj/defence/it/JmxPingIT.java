package uk.gov.moj.defence.it;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.mbean.CommandRunMode.FORCED;
import static uk.gov.justice.services.jmx.system.command.client.connection.JmxParametersBuilder.jmxParameters;
import static uk.gov.justice.services.management.ping.commands.PingCommand.PING;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.jmx.api.domain.SystemCommandStatus;
import uk.gov.justice.services.jmx.api.mbean.SystemCommanderMBean;
import uk.gov.justice.services.jmx.system.command.client.SystemCommanderClient;
import uk.gov.justice.services.jmx.system.command.client.TestSystemCommanderClientFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class JmxPingIT {

    private static final String HOST = getHost();
    private static final int PORT = 9990;
    private static final String CONTEXT = "defence";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static final UUID NULL_COMMAND_RUNTIME_ID = null;
    private static final String NULL_COMMAND_RUNTIME_STRING = null;

    private final TestSystemCommanderClientFactory systemCommanderClientFactory = new TestSystemCommanderClientFactory();

    @Test
    public void shouldSuccessfullyCallPingUsingJmx() {

        final JmxParameters jmxParameters = jmxParameters()
                .withContextName(CONTEXT)
                .withHost(HOST)
                .withPort(PORT)
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .build();
        try (final SystemCommanderClient systemCommanderClient = systemCommanderClientFactory.create(jmxParameters)) {
            final SystemCommanderMBean systemCommanderMBean = systemCommanderClient.getRemote(CONTEXT);
            final UUID commandId = systemCommanderMBean.call(
                    PING,
                    NULL_COMMAND_RUNTIME_ID,
                    NULL_COMMAND_RUNTIME_STRING,
                    FORCED.isGuarded());

            assertThat(commandId, is(notNullValue()));

            final SystemCommandStatus commandStatus = systemCommanderMBean.getCommandStatus(commandId);
            assertThat(commandStatus.getCommandId(), is(commandId));
            assertThat(commandStatus.getCommandState(), is(COMMAND_COMPLETE));
        }
    }
}
