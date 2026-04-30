package uk.gov.moj.defence.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static jakarta.json.Json.createArrayBuilder;
import static jakarta.json.Json.createObjectBuilder;
import static org.hamcrest.core.IsNull.notNullValue;
import static uk.gov.moj.defence.helper.OrganisationAccessHelper.verifyOrganisationAssignedToTheTheCase;
import static uk.gov.moj.defence.helper.CaseAssignmentHelper.assignCaseToAdvocate;
import static uk.gov.moj.defence.stub.ProgressionServiceStub.stubProgressionService;
import static uk.gov.moj.defence.stub.ReferenceDataStub.stubQueryProsecutorData;
import static uk.gov.moj.defence.util.AccessControlStub.stubAccessControl;
import static uk.gov.moj.defence.util.AccessControlStub.stubAccessControlForAllUsers;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationDetails;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationDetailsForSpecificUser;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationDetailsForUser;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationQuery;
import static uk.gov.moj.defence.util.UsersGroupStub.stubUserPermissions;
import static uk.gov.moj.defence.util.UsersGroupStub.stubUsersGroupsSearchUsersForEmail;
import static uk.gov.moj.defence.util.UsersGroupStub.stubUsersGroupsSearchUsersForUserId;
import static uk.gov.moj.defence.util.WiremockHelper.resetWiremock;

import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrganisationAccessQueryIT {

    private final DatabaseCleaner databaseCleaner = new DatabaseCleaner();

    private static final String ASSIGNEE_EMAIL = "assignee@hmcts.net";
    private static final String ASSIGNEE_TWO_EMAIL = "assignee2@hmcts.net";
    private static final String ORGANISATION_NAME = "organisationName1";

    private UUID caseId;
    private UUID assignerUserId;
    private UUID assigneeUserId;
    private UUID assigneeTwoUserId;
    private UUID organisationId;

    @BeforeEach
    public void cleanDatabase() {
        databaseCleaner.resetEventSubscriptionStatusTable("defence");
        databaseCleaner.cleanStreamBufferTable("defence");
        databaseCleaner.cleanStreamStatusTable("defence");
        databaseCleaner.cleanEventStoreTables("defence");
        databaseCleaner.cleanProcessedEventTable("defence");
        databaseCleaner.cleanViewStoreTables("defence",
                "prosecution_advocate_access",
                "prosecution_organisation_access",
                "advocate_access",
                "defence_grant_access",
                "defendant_allocation_pleas",
                "defendant_allocation",
                "allegation",
                "instruction",
                "defence_association",
                "defence_association_defendant",
                "defence_case",
                "idpc_access_history",
                "idpc_details",
                "assignment_user_details",
                "defence_user_details",
                "organisation_details",
                "defence_client");
    }

    @BeforeEach
    public void setup() {
        caseId = randomUUID();
        assignerUserId = randomUUID();
        assigneeUserId = randomUUID();
        assigneeTwoUserId = randomUUID();
        organisationId = randomUUID();

        resetWiremock();
        stubProgressionService(caseId);
        stubQueryProsecutorData(true, true);
        stubAccessControlForAllUsers(true, assignerUserId, "CPS");
        stubAccessControl(true, assigneeUserId, "Defence Lawyers", "Chambers Admin");
        stubAccessControl(true, assigneeTwoUserId, "Defence Lawyers", "Chambers Admin");
        stubUsersGroupsSearchUsersForEmail(assigneeUserId.toString(), ASSIGNEE_EMAIL, "stub-data/usersgroup-service/user-for-exist-email.json");
        stubUsersGroupsSearchUsersForEmail(assigneeTwoUserId.toString(), ASSIGNEE_TWO_EMAIL, "stub-data/usersgroup-service/user-for-exist-email.json");
        stubUsersGroupsSearchUsersForUserId(assignerUserId.toString(), "stub-data/usersgroup-service/user-for-exist-email.json");
        stubGetOrganisationQuery(assignerUserId.toString(), organisationId.toString(), ORGANISATION_NAME);
        stubGetOrganisationDetailsForUser(randomUUID(), organisationId);
        stubGetOrganisationDetails(organisationId.toString(), ORGANISATION_NAME);
        stubUserPermissions();
    }

    @Test
    public void shouldSaveProsecutionOrganisationAssignedToTheCaseAndRetrieve() {
        final String payload = createObjectBuilder()
                .add("assigneeEmailId", ASSIGNEE_EMAIL)
                .add("assignorId", assignerUserId.toString())
                .add("assignorOrganisationId", randomUUID().toString())
                .add("caseIds", createArrayBuilder().add(caseId.toString()))
                .build()
                .toString();

        assignCaseToAdvocate(payload, assignerUserId.toString());

        List<Matcher<? super ReadContext>> matchers = ImmutableList.<Matcher<? super ReadContext>>builder()
                .add(withJsonPath("$.assignees[0].assigneeUserId", IsEqual.equalTo(assigneeUserId.toString())))
                .add(withJsonPath("$.assignees[0].assigneeOrganisationId", IsEqual.equalTo(organisationId.toString())))
                .add(withJsonPath("$.assignees[0].representation", IsEqual.equalTo("PROSECUTION")))
                .add(withJsonPath("$.assignees[0].representing", IsEqual.equalTo("CPS")))
                .add(withJsonPath("$.assignees[0].assignedDate", notNullValue()))
                .build();

        verifyOrganisationAssignedToTheTheCase(caseId.toString(), assignerUserId.toString(), matchers);
    }

    @Test
    public void shouldSaveDifferentProsecutionOrganisationsAssignedToTheSameCaseAndRetrieve() {
        final UUID anotherOrganisationId = randomUUID();

        final String payloadOne = createObjectBuilder()
                .add("assigneeEmailId", ASSIGNEE_EMAIL)
                .add("assignorId", assignerUserId.toString())
                .add("assignorOrganisationId", randomUUID().toString())
                .add("caseIds", createArrayBuilder().add(caseId.toString()))
                .build()
                .toString();

        assignCaseToAdvocate(payloadOne, assignerUserId.toString());

        verifyOrganisationAssignedToTheTheCase(caseId.toString(), assignerUserId.toString(),
                ImmutableList.<Matcher<? super ReadContext>>builder()
                        .add(withJsonPath("$.assignees[0].assigneeUserId", IsEqual.equalTo(assigneeUserId.toString())))
                        .add(withJsonPath("$.assignees[0].assigneeOrganisationId", IsEqual.equalTo(organisationId.toString())))
                        .build());

        stubGetOrganisationDetailsForSpecificUser(assigneeTwoUserId, anotherOrganisationId);

        final String payloadTwo = createObjectBuilder()
                .add("assigneeEmailId", ASSIGNEE_TWO_EMAIL)
                .add("assignorId", assignerUserId.toString())
                .add("assignorOrganisationId", randomUUID().toString())
                .add("caseIds", createArrayBuilder().add(caseId.toString()))
                .build()
                .toString();

        assignCaseToAdvocate(payloadTwo, assignerUserId.toString());

        List<Matcher<? super ReadContext>> multiOrgMatchers = ImmutableList.<Matcher<? super ReadContext>>builder()
                .add(withJsonPath("$.assignees[0].representation", IsEqual.equalTo("PROSECUTION")))
                .add(withJsonPath("$.assignees[0].representing", IsEqual.equalTo("CPS")))
                .add(withJsonPath("$.assignees[0].assignedDate", notNullValue()))
                .add(withJsonPath("$.assignees[1].representation", IsEqual.equalTo("PROSECUTION")))
                .add(withJsonPath("$.assignees[1].representing", IsEqual.equalTo("CPS")))
                .add(withJsonPath("$.assignees[1].assignedDate", notNullValue()))
                .build();

        verifyOrganisationAssignedToTheTheCase(caseId.toString(), assignerUserId.toString(), multiOrgMatchers);
    }
}
