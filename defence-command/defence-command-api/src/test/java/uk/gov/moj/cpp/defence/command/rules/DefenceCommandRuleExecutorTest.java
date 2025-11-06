package uk.gov.moj.cpp.defence.command.rules;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.accesscontrol.common.providers.UserAndGroupProvider;
import uk.gov.moj.cpp.accesscontrol.drools.Action;
import uk.gov.moj.cpp.accesscontrol.test.utils.BaseDroolsAccessControlTest;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.runtime.ExecutionResults;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefenceCommandRuleExecutorTest extends BaseDroolsAccessControlTest {

    protected Action action;

    @Mock
    protected UserAndGroupProvider userAndGroupProvider;

    public DefenceCommandRuleExecutorTest() {
        super("COMMAND_API_SESSION");
    }

    @Override
    protected Map<Class<?>, Object> getProviderMocks() {
        return singletonMap(UserAndGroupProvider.class, userAndGroupProvider);
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
            action = createActionFor(ruleTest.actionName);
            when(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, ruleTest.allowedUserGroups)).thenReturn(false);
            final ExecutionResults executionResults = executeRulesWith(action);
            assertFailureOutcome(executionResults);
            verify(userAndGroupProvider).isMemberOfAnyOfTheSuppliedGroups(action, ruleTest.allowedUserGroups);
            verifyNoMoreInteractions(userAndGroupProvider);
        });
    }

    public enum DefenceRules {

        AssociateDefenceOrganisation("defence.associate-defence-organisation", "System Users" , "Chambers Clerk", "Chambers Admin", "Defence Lawyers", "Advocates"),
        DisassociateDefenceOrganisation("defence.disassociate-defence-organisation", "Court Associate","Court Clerks", "Court Administrators", "Crown Court Admin", "Listing Officers", "Legal Advisers", "System Users", "Chambers Clerk", "Chambers Admin", "Defence Lawyers", "Advocates");

        private final String actionName;
        private final String[] allowedUserGroups;

        DefenceRules(final String actionName, final String... allowedUserGroups) {
            this.actionName = actionName;
            this.allowedUserGroups = allowedUserGroups;
        }
    }
}
