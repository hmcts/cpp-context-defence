package uk.gov.moj.cpp.defence.query.api.accesscontrol;


import static java.util.Collections.singletonMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder;
import uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory;
import uk.gov.moj.cpp.accesscontrol.common.providers.UserAndGroupProvider;
import uk.gov.moj.cpp.accesscontrol.drools.Action;
import uk.gov.moj.cpp.accesscontrol.test.utils.BaseDroolsAccessControlTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.ExecutionResults;
import org.mockito.Mock;

@SuppressWarnings("java:S5976")
public class QueryDefenceApiAccessControlTest extends BaseDroolsAccessControlTest {

    private Action action;

    public QueryDefenceApiAccessControlTest() {
        super("QUERY_API_SESSION");
    }

    @Mock
    private UserAndGroupProvider userAndGroupProvider;

    @Test
    public void shouldAllowAuthorisedUserToQueryClientByCriteria() {
        action = createActionFor("defence.query.defence-client-id");
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getQueryClientByOrganisationIdGroups()))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToQueryClientByCriteria() {
        action = createActionFor("defence.query.defence-client-id");

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToQueryIDPC() {
        action = createActionFor("defence.query.defence-client-idpc");
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getQueryClientByCriteriaGroups()))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToQueryIDPC() {
        action = createActionFor("defence.query.defence-client-idpc");

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToIssueRecordIDPCAccess() {
        action = createActionFor("defence.query.record-idpc-access");
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getQueryClientByCriteriaGroups()))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToIssueRecordIDPCAccess() {
        action = createActionFor("defence.query.record-idpc-access");

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }


    @Test
    public void shouldAllowToQueryDefenceClient_IfHasTheSuppliedPermissionBasedOnActionRole() throws JsonProcessingException {
        Map<String, String> metadata = new HashMap();
        metadata.putIfAbsent("id", UUID.randomUUID().toString());
        metadata.putIfAbsent("name", "defence.query.defence-client-allegations");
        final JsonEnvelope envelope = JsonEnvelopeBuilder.envelope().with(MetadataBuilderFactory.metadataOf(UUID.randomUUID().toString(), (String)metadata.get("name"))).withPayloadOf(UUID.randomUUID().toString(),"defenceClientId").build();
        action = new Action(envelope);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getQueryClientByCriteriaGroups()))
                .willReturn(false);
        given(userAndGroupProvider.hasPermission(action, ExpectedPermissionConstants.defenceClientViewAction(action))).willReturn(true);
        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
        verify(userAndGroupProvider, times(1)).hasPermission(action,ExpectedPermissionConstants.defenceClientViewAction(action));

    }

    @Test
    public void shouldAllowToQueryDefenceClient_IfHasTheSuppliedPermissionBasedOnUserGroup() throws JsonProcessingException {
        Map<String, String> metadata = new HashMap();
        metadata.putIfAbsent("id", UUID.randomUUID().toString());
        metadata.putIfAbsent("name", "defence.query.defence-client-allegations");
        final JsonEnvelope envelope = JsonEnvelopeBuilder.envelope().with(MetadataBuilderFactory.metadataOf(UUID.randomUUID().toString(), (String)metadata.get("name"))).withPayloadOf(UUID.randomUUID().toString(),"defenceClientId").build();
        action = new Action(envelope);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getQueryClientByCriteriaGroups()))
                .willReturn(true);
        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }



    @Test
    public void shouldAllowToQueryCaseByDefendant() {
        action = createActionFor("defence.query.get-case-by-person-defendant");
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getQueryClientByOrganisationIdGroups()))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowToQueryCaseByDefendant() {
        action = createActionFor("defence.query.get-case-by-person-defendant");
        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowToQueryCaseByOrganisationDefendant() {
        action = createActionFor("defence.query.get-case-by-organisation-defendant");
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getQueryClientByOrganisationIdGroups()))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowToQueryCaseByOrganisationDefendant() {
        action = createActionFor("defence.query.get-case-by-organisation-defendant");
        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Override
    protected Map<Class<?>, Object> getProviderMocks() {
        return singletonMap(UserAndGroupProvider.class, userAndGroupProvider);
    }

    @Override
    protected Action createActionFor(final Map<String, String> metadata) {
        JsonEnvelopeBuilder jsonEnvelopeBuilder = JsonEnvelopeBuilder.envelope().withPayloadOf( UUID.randomUUID().toString(),"defenceClientId");;
        return new Action(jsonEnvelopeBuilder.with(metadataOf(UUID.randomUUID().toString(), metadata.get("name"))).build());
    }
}
