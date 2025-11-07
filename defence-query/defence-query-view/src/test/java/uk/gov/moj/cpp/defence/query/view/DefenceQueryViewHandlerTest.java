package uk.gov.moj.cpp.defence.query.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import org.junit.jupiter.api.Test;

public class DefenceQueryViewHandlerTest {

    @Test
    public void shouldHandleSearchClientByCriteria() {

        assertThat(new DefenceQueryView(), isHandler(QUERY_VIEW)
                .with(method("findClientByCriteria")
                        .thatHandles("defence.defence-client-idpc-access-orgs")
                ));
    }

    @Test
    public void shouldHandleSearchAllegationsByClientId() {

        assertThat(new DefenceQueryView(), isHandler(QUERY_VIEW)
                .with(method("findAllegationsByDefenceClientId")
                        .thatHandles("defence.defence-client-allegations")
                ));
    }

    @Test
    public void shouldHandleDefenceClientIDPC() {

        assertThat(new DefenceQueryView(), isHandler(QUERY_VIEW)
                .with(method("getDefenceClientIdpc")
                        .thatHandles("defence.query.view.defence-client-idpc")
                ));
    }


}