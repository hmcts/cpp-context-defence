package uk.gov.moj.defence.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static uk.gov.moj.defence.helper.AdvocateAccessHelper.verifyUserAssignmentToTheCase;
import static uk.gov.moj.defence.util.AccessControlStub.stubAccessControl;
import static uk.gov.moj.defence.util.AuthorisationServiceStub.stubCapabilities;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationDetails;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationDetailsForUser;
import static uk.gov.moj.defence.util.UsersGroupStub.stubUserPermissions;
import static uk.gov.moj.defence.util.WiremockHelper.resetWiremock;

import uk.gov.moj.defence.helper.AdvocateAccessHelper;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CpsCaseAccessQueryIT {
    private final UUID userId = randomUUID();
    private final UUID organisationId = UUID.randomUUID();

    private String caseId;
    private String assigneeUserId;
    private String assigneeOrgId;
    private String assignorUserId;
    private String assignorOrgId;

    private AdvocateAccessHelper advocateAccessHelper;

    @BeforeEach
    public void setup() {
        advocateAccessHelper = new AdvocateAccessHelper();

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
    public void shouldSaveProsecutionAdvocateCaseAssignmentAndRetrieve() {
        stubGetOrganisationDetailsForUser(userId, organisationId);

        advocateAccessHelper.sendCaseAssignedToAdvocateEvent(caseId, assigneeUserId, assigneeOrgId,
                assignorUserId, assignorOrgId);

        List<Matcher<? super ReadContext>> matchers = ImmutableList.<Matcher<? super ReadContext>>builder()
                .add(withJsonPath("$.assignees[0].assigneeUserId", IsEqual.equalTo(assigneeUserId)))
                .add(withJsonPath("$.assignees[0].assigneeOrganisationId", IsEqual.equalTo(assigneeOrgId)))
                .add(withJsonPath("$.assignees[0].representation", IsEqual.equalTo("PROSECUTION")))
                .add(withJsonPath("$.assignees[0].representing", IsEqual.equalTo("CPS")))
                .add(withJsonPath("$.assignees[0].assignedDate", IsNull.notNullValue()))
                .add(withJsonPath("$.assignees[0].address.address1", IsEqual.equalTo("Legal House")))
                .add(withJsonPath("$.assignees[0].address.address2", IsEqual.equalTo("15 Sewell Street")))
                .add(withJsonPath("$.assignees[0].address.address3", IsEqual.equalTo("Hammersmith")))
                .add(withJsonPath("$.assignees[0].address.address4", IsEqual.equalTo("London")))
                .add(withJsonPath("$.assignees[0].address.addressPostcode", IsEqual.equalTo("SE14 2AB")))
                .build();

        verifyUserAssignmentToTheCase(caseId, userId.toString(), matchers);
    }

    @Test
    public void shouldSaveProsecutionAdvocateCaseAssignmentsForSameOrganisationAndRetrieve() {
        stubGetOrganisationDetailsForUser(userId, organisationId);


        advocateAccessHelper.sendCaseAssignedToAdvocateEvent(caseId, assigneeUserId, assigneeOrgId,
                assignorUserId, assignorOrgId);

        List<Matcher<? super ReadContext>> matchers = ImmutableList.<Matcher<? super ReadContext>>builder()
                .add(withJsonPath("$.assignees[0].assigneeUserId", IsEqual.equalTo(assigneeUserId)))
                .add(withJsonPath("$.assignees[0].assigneeOrganisationId", IsEqual.equalTo(assigneeOrgId)))
                .build();

        verifyUserAssignmentToTheCase(caseId, userId.toString(), matchers);

        String anotherAdvocateFromSameOrg = randomUUID().toString();
        advocateAccessHelper.sendCaseAssignedToAdvocateEvent(caseId, anotherAdvocateFromSameOrg, assigneeOrgId,
                randomUUID().toString(), randomUUID().toString());
        List<Matcher<? super ReadContext>> multiAdvocatesMatchers = ImmutableList.<Matcher<? super ReadContext>>builder()

                .add(withJsonPath("$.assignees[0].assigneeUserId", IsEqual.equalTo(assigneeUserId)))
                .add(withJsonPath("$.assignees[0].assigneeOrganisationId", IsEqual.equalTo(assigneeOrgId)))

                .add(withJsonPath("$.assignees[1].assigneeUserId", IsEqual.equalTo(anotherAdvocateFromSameOrg)))
                .add(withJsonPath("$.assignees[1].assigneeOrganisationId", IsEqual.equalTo(assigneeOrgId)))
                .build();

        verifyUserAssignmentToTheCase(caseId, userId.toString(), multiAdvocatesMatchers);
    }

}
