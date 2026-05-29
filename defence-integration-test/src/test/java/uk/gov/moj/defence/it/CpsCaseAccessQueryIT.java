package uk.gov.moj.defence.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static jakarta.json.Json.createArrayBuilder;
import static jakarta.json.Json.createObjectBuilder;
import static uk.gov.moj.defence.helper.AdvocateAccessHelper.verifyUserAssignmentToTheCase;
import static uk.gov.moj.defence.helper.CaseAssignmentHelper.assignCaseToAdvocate;
import static uk.gov.moj.defence.stub.ProgressionServiceStub.stubProgressionService;
import static uk.gov.moj.defence.stub.ReferenceDataStub.stubQueryProsecutorData;
import static uk.gov.moj.defence.util.AccessControlStub.stubAccessControl;
import static uk.gov.moj.defence.util.AccessControlStub.stubAccessControlForAllUsers;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationDetails;
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
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CpsCaseAccessQueryIT {

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
        stubAccessControl(true, assigneeUserId, "Advocates", "Chambers Admin");
        stubAccessControl(true, assigneeTwoUserId, "Advocates", "Chambers Admin");
        stubUsersGroupsSearchUsersForEmail(assigneeUserId.toString(), ASSIGNEE_EMAIL, "stub-data/usersgroup-service/user-for-exist-email.json");
        stubUsersGroupsSearchUsersForEmail(assigneeTwoUserId.toString(), ASSIGNEE_TWO_EMAIL, "stub-data/usersgroup-service/user-for-exist-email.json");
        stubUsersGroupsSearchUsersForUserId(assignerUserId.toString(), "stub-data/usersgroup-service/user-for-exist-email.json");
        stubGetOrganisationQuery(assignerUserId.toString(), organisationId.toString(), ORGANISATION_NAME);
        stubGetOrganisationDetailsForUser(randomUUID(), organisationId);
        stubGetOrganisationDetails(organisationId.toString(), ORGANISATION_NAME);
        stubUserPermissions();
    }

    @Test
    public void shouldSaveProsecutionAdvocateCaseAssignmentAndRetrieve() {
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
                .add(withJsonPath("$.assignees[0].assignedDate", IsNull.notNullValue()))
                .add(withJsonPath("$.assignees[0].address.address1", IsEqual.equalTo("Legal House")))
                .add(withJsonPath("$.assignees[0].address.address2", IsEqual.equalTo("15 Sewell Street")))
                .add(withJsonPath("$.assignees[0].address.address3", IsEqual.equalTo("Hammersmith")))
                .add(withJsonPath("$.assignees[0].address.address4", IsEqual.equalTo("London")))
                .add(withJsonPath("$.assignees[0].address.addressPostcode", IsEqual.equalTo("SE14 2AB")))
                .build();

        verifyUserAssignmentToTheCase(caseId.toString(), assigneeUserId.toString(), matchers);
    }

    @Test
    public void shouldSaveProsecutionAdvocateCaseAssignmentsForSameOrganisationAndRetrieve() {
        final String payloadOne = createObjectBuilder()
                .add("assigneeEmailId", ASSIGNEE_EMAIL)
                .add("assignorId", assignerUserId.toString())
                .add("assignorOrganisationId", randomUUID().toString())
                .add("caseIds", createArrayBuilder().add(caseId.toString()))
                .build()
                .toString();

        assignCaseToAdvocate(payloadOne, assignerUserId.toString());

        List<Matcher<? super ReadContext>> firstAssigneeMatchers = ImmutableList.<Matcher<? super ReadContext>>builder()
                .add(withJsonPath("$.assignees[0].assigneeUserId", IsEqual.equalTo(assigneeUserId.toString())))
                .add(withJsonPath("$.assignees[0].assigneeOrganisationId", IsEqual.equalTo(organisationId.toString())))
                .build();

        verifyUserAssignmentToTheCase(caseId.toString(), assigneeUserId.toString(), firstAssigneeMatchers);

        final String payloadTwo = createObjectBuilder()
                .add("assigneeEmailId", ASSIGNEE_TWO_EMAIL)
                .add("assignorId", assignerUserId.toString())
                .add("assignorOrganisationId", randomUUID().toString())
                .add("caseIds", createArrayBuilder().add(caseId.toString()))
                .build()
                .toString();

        assignCaseToAdvocate(payloadTwo, assignerUserId.toString());

        List<Matcher<? super ReadContext>> multiAdvocatesMatchers = ImmutableList.<Matcher<? super ReadContext>>builder()
                .add(withJsonPath("$.assignees[0].assigneeUserId", IsEqual.equalTo(assigneeTwoUserId.toString())))
                .add(withJsonPath("$.assignees[0].assigneeOrganisationId", IsEqual.equalTo(organisationId.toString())))
                .add(withJsonPath("$.assignees[1].assigneeUserId", IsEqual.equalTo(assigneeUserId.toString())))
                .add(withJsonPath("$.assignees[1].assigneeOrganisationId", IsEqual.equalTo(organisationId.toString())))
                .build();

        verifyUserAssignmentToTheCase(caseId.toString(), assigneeUserId.toString(), multiAdvocatesMatchers);
    }
}
