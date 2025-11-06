package uk.gov.moj.defence.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static uk.gov.moj.defence.helper.OrganisationAccessHelper.verifyOrganisationAssignedToTheTheCase;
import static uk.gov.moj.defence.util.AccessControlStub.stubAccessControl;
import static uk.gov.moj.defence.util.AuthorisationServiceStub.stubCapabilities;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationDetails;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationDetailsForUser;
import static uk.gov.moj.defence.util.UsersGroupStub.stubUserPermissions;
import static uk.gov.moj.defence.util.WiremockHelper.resetWiremock;

import uk.gov.moj.defence.helper.OrganisationAccessHelper;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Matcher;
import org.hamcrest.core.AnyOf;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrganisationAccessQueryIT {
    private final UUID userId = randomUUID();
    private final UUID organisationId = UUID.randomUUID();

    private String caseId;
    private String assigneeUserId;
    private String assigneeOrgId;
    private String assignorUserId;
    private String assignorOrgId;

    @BeforeEach
    public void setup() {
        caseId = randomUUID().toString();
        assigneeUserId = randomUUID().toString();
        assigneeOrgId = randomUUID().toString();
        assignorUserId = randomUUID().toString();
        assignorOrgId = randomUUID().toString();

        setupExternalInterfaceMocks();
        stubUserPermissions();
    }

    protected void setupExternalInterfaceMocks() {
        resetWiremock();
        stubGetOrganisationDetailsForUser(userId, organisationId);
        stubGetOrganisationDetails(assigneeOrgId, "organisationName1");
        stubCapabilities();
        stubAccessControl(true, userId, "Defence Lawyers");
    }

    @Test
    public void shouldSaveProsecutionOrganisationAssignedToTheCaseAndRetrieve() {
        stubGetOrganisationDetailsForUser(userId, organisationId);

        OrganisationAccessHelper organisationAccessHelper = new OrganisationAccessHelper();
        organisationAccessHelper.sendCaseAssignedToOrganisationEvent(caseId, assigneeUserId, assigneeOrgId,
                assignorUserId, assignorOrgId);

        verifyOrganisationAssignedToTheTheCase(caseId, userId.toString(), getOrgMatcher());
    }

    @Test
    public void shouldSaveDifferentProsecutionOrganisationsAssignedToTheSameCaseAndRetrieve() {
        stubGetOrganisationDetailsForUser(userId, organisationId);

        //given prosecution organisation assignment event sent
        OrganisationAccessHelper organisationAccessHelper = new OrganisationAccessHelper();
        organisationAccessHelper.sendCaseAssignedToOrganisationEvent(caseId, assigneeUserId, assigneeOrgId,
                assignorUserId, assignorOrgId);

        //and verify org assigned to the case
        verifyOrganisationAssignedToTheTheCase(caseId, userId.toString(), getOrgMatcher());

        //when another prosecution organisation assignment event sent
        String anotherAssigneeOrgId = randomUUID().toString();
        String anotherAssigneeUserId = randomUUID().toString();
        stubGetOrganisationDetails(anotherAssigneeOrgId, "organisationName2");
        organisationAccessHelper.sendCaseAssignedToOrganisationEvent(caseId, anotherAssigneeUserId, anotherAssigneeOrgId,
                randomUUID().toString(), randomUUID().toString());

        //and verify another org assigned to the case
        List<Matcher<? super ReadContext>> multiOrgMatchers = ImmutableList.<Matcher<? super ReadContext>>builder()
                .add(withJsonPath("$.assignees[0].assigneeUserId", AnyOf.anyOf(IsEqual.equalTo(assigneeUserId), IsEqual.equalTo(anotherAssigneeUserId))))
                .add(withJsonPath("$.assignees[0].assigneeOrganisationId", AnyOf.anyOf(IsEqual.equalTo(assigneeOrgId), IsEqual.equalTo(anotherAssigneeOrgId))))
                .add(withJsonPath("$.assignees[0].representation",  IsEqual.equalTo("PROSECUTION")))
                .add(withJsonPath("$.assignees[0].representing", IsEqual.equalTo("CPS")))
                .add(withJsonPath("$.assignees[0].assignedDate", IsNull.notNullValue()))

                .add(withJsonPath("$.assignees[1].assigneeUserId", AnyOf.anyOf(IsEqual.equalTo(assigneeUserId), IsEqual.equalTo(anotherAssigneeUserId))))
                .add(withJsonPath("$.assignees[1].assigneeOrganisationId", AnyOf.anyOf(IsEqual.equalTo(assigneeOrgId), IsEqual.equalTo(anotherAssigneeOrgId))))
                .add(withJsonPath("$.assignees[1].representation", IsEqual.equalTo("PROSECUTION")))
                .add(withJsonPath("$.assignees[1].representing", IsEqual.equalTo("CPS")))
                .add(withJsonPath("$.assignees[1].assignedDate", IsNull.notNullValue()))

                .build();
        verifyOrganisationAssignedToTheTheCase(caseId, userId.toString(), multiOrgMatchers);
    }

    private ImmutableList<Matcher<? super ReadContext>> getOrgMatcher() {
        return ImmutableList.<Matcher<? super ReadContext>>builder()
                .add(withJsonPath("$.assignees[0].assigneeUserId", IsEqual.equalTo(assigneeUserId)))
                .add(withJsonPath("$.assignees[0].assigneeOrganisationId", IsEqual.equalTo(assigneeOrgId)))
                .add(withJsonPath("$.assignees[0].representation", IsEqual.equalTo("PROSECUTION")))
                .add(withJsonPath("$.assignees[0].representing", IsEqual.equalTo("CPS")))
                .add(withJsonPath("$.assignees[0].assignedDate", IsNull.notNullValue()))
                .build();
    }

}
