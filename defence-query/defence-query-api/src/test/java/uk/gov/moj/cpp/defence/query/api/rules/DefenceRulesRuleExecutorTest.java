package uk.gov.moj.cpp.defence.query.api.rules;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.defence.query.api.accesscontrol.RuleConstants.getQueryClientByCriteriaGroups;
import static uk.gov.moj.cpp.defence.query.api.accesscontrol.RuleConstants.getQueryClientByOrganisationIdGroups;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder;
import uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory;
import uk.gov.moj.cpp.accesscontrol.common.providers.UserAndGroupProvider;
import uk.gov.moj.cpp.accesscontrol.drools.Action;
import uk.gov.moj.cpp.accesscontrol.test.utils.BaseDroolsAccessControlTest;
import uk.gov.moj.cpp.defence.query.api.accesscontrol.RuleConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.kie.api.runtime.ExecutionResults;
import org.mockito.Mock;

public class DefenceRulesRuleExecutorTest extends BaseDroolsAccessControlTest {

    protected Action action;
    @Mock
    protected UserAndGroupProvider userAndGroupProvider;

    public DefenceRulesRuleExecutorTest() {
        super("QUERY_API_SESSION");
    }

    @Override
    protected Map<Class<?>, Object> getProviderMocks() {
        return Collections.singletonMap(UserAndGroupProvider.class, userAndGroupProvider);
    }

    @Test
    public void whenUserIsAMemberOfAllowedUserGroups() {
        Arrays.stream(DefenceRules.values()).forEach(ruleTest -> {
            action = createActionFor(ruleTest.actionName);
            when(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, ruleTest.allowedUserGroups)).thenReturn(true);
            final ExecutionResults executionResults = executeRulesWith(action);
            assertSuccessfulOutcome(executionResults);
            verify(userAndGroupProvider).isMemberOfAnyOfTheSuppliedGroups(action, ruleTest.allowedUserGroups);
            verifyNoMoreInteractions(userAndGroupProvider);
        });
    }


    @Test
    public void whenUserIsNotAMemberOfAllowedUserGroups_thenFailUpload() throws Exception {
        Arrays.stream(DefenceRules.values()).forEach(ruleTest -> {
            Map<String, String> metadata = new HashMap<>();
            metadata.putIfAbsent("id", UUID.randomUUID().toString());
            metadata.putIfAbsent("name", ruleTest.actionName);
            final JsonEnvelope envelope = JsonEnvelopeBuilder.envelope().with(MetadataBuilderFactory.metadataOf(UUID.randomUUID().toString(), metadata.get("name"))).withPayloadOf(UUID.randomUUID().toString(),"defenceClientId").build();
            action = new Action(envelope);
            when(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, ruleTest.allowedUserGroups)).thenReturn(false);
            lenient().when(userAndGroupProvider.hasPermission(any(), any())).thenReturn(false);
            final ExecutionResults executionResults = executeRulesWith(action);
            assertFailureOutcome(executionResults);
            verify(userAndGroupProvider).isMemberOfAnyOfTheSuppliedGroups(action, ruleTest.allowedUserGroups);
        });
    }

    public enum DefenceRules {
        GetDefenceClient("defence.query.defence-client-id", getQueryClientByOrganisationIdGroups()),
        GetOrganisationDefenceClient("defence.query.defence-client-organisation-id", RuleConstants.getQueryClientByOrganisationIdGroups()),
        GetDefenceClientAllegation("defence.query.defence-client-allegations", getQueryClientByCriteriaGroups()),
        GetDefenceClientIDPC("defence.query.defence-client-idpc", getQueryClientByCriteriaGroups()),
        GetDefenceClientIdpcMetaData("defence.query.defence-client-idpc-metadata", getQueryClientByCriteriaGroups()),
        GetDefenceClientIdpcAccess("defence.query.record-idpc-access", getQueryClientByCriteriaGroups()),
        GetDefenceClientByDefendantId("defence.query.defence-client-defendantId", RuleConstants.getQueryClientOrganisationGroups()),
        GetCaseAdvocateAccessAssignees("defence.query.case-cps-assignees", RuleConstants.getQueryClientOrganisationGroups()),
        GetDefenceClientByOrganisation("defence.query.associated-organisation", RuleConstants.getQueryClientAssociatedOrganisationGroups());
        private final String actionName;
        private final String[] allowedUserGroups;
        DefenceRules(final String actionName, final String... allowedUserGroups) {
            this.actionName = actionName;
            this.allowedUserGroups = allowedUserGroups;
        }
    }

    @Override
    protected Action createActionFor(final Map<String, String> metadata) {
        JsonEnvelopeBuilder jsonEnvelopeBuilder = JsonEnvelopeBuilder.envelope().withPayloadOf( UUID.randomUUID().toString(),"defenceClientId");;
        return new Action(jsonEnvelopeBuilder.with(metadataOf(UUID.randomUUID().toString(), metadata.get("name"))).build());
    }
}
