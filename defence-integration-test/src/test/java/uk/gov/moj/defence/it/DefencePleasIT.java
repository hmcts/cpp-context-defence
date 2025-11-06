package uk.gov.moj.defence.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.LocalDate.parse;
import static java.time.Period.ofYears;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.regex.Pattern.compile;
import static org.hamcrest.CoreMatchers.is;
import static uk.gov.justice.services.test.utils.core.random.DateGenerator.Direction.FUTURE;
import static uk.gov.moj.defence.domain.common.UrnRegex.URN_PATTERN;
import static uk.gov.moj.defence.helper.DefenceAssociationHelper.addPleaAllocation;
import static uk.gov.moj.defence.helper.DefenceAssociationHelper.addPleaAllocationForOrganisation;
import static uk.gov.moj.defence.helper.DefenceAssociationHelper.associateOrganisation;
import static uk.gov.moj.defence.helper.DefenceAssociationHelper.updatePleaAllocation;
import static uk.gov.moj.defence.helper.DefenceAssociationHelper.verifyDefenceOrganisationAssociatedDataPersisted;
import static uk.gov.moj.defence.helper.PleasHelper.pollForDefencePleas;
import static uk.gov.moj.defence.util.AccessControlStub.stubAccessControlForAllUsers;
import static uk.gov.moj.defence.util.PleaAccessControl.mockDefenceUserPleaAccessControl;
import static uk.gov.moj.defence.util.ProsecutionCaseQueryStub.stubForProsecutionCaseQuery;
import static uk.gov.moj.defence.util.ReferenceDataOffencesQueryStub.stubForReferenceDataQueryOffence;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationDetails;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationDetailsForUser;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationNamesForIds;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetUsersAndGroupsQueryForDefenceUsers;
import static uk.gov.moj.defence.util.UsersGroupStub.stubUserPermissions;
import static uk.gov.moj.defence.util.UsersGroupStub.stubUsersGroupsPermission;
import static uk.gov.moj.defence.util.WiremockHelper.resetWiremock;

import uk.gov.justice.json.generator.value.string.RegexGenerator;
import uk.gov.justice.json.generator.value.string.SimpleStringGenerator;
import uk.gov.justice.services.test.utils.core.random.LocalDateGenerator;
import uk.gov.moj.defence.helper.CreateProsecutionCaseHelper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefencePleasIT {


    private final CreateProsecutionCaseHelper createProsecutionCaseHelper = new CreateProsecutionCaseHelper();

    private static final String UPDATE_PLEA_ALLOCATION_REQUEST_WITH_DEFENDANT_DETAILS_CORRECTION_TEMPLATE_NAME = "stub-data/commands-payloads/defence.update-offence-pleas-with-def-details.json";
    private static final String UPDATE_PLEA_ALLOCATION_REQUEST_WITHOUT_DEFENDANT_DETAILS_CORRECTION_TEMPLATE_NAME = "stub-data/commands-payloads/defence.update-offence-pleas-without-def-details.json";

    private UUID userId;
    private String organisationId;
    private String newOrganisationId;
    private String organisationName;
    private String newOrganisationName;
    private UUID caseId;
    private String urn;
    private String firstName;
    private String lastName;
    private String defendantId;
    private String dateOfBirth;
    private static final RegexGenerator regexGenerator = new RegexGenerator(compile(URN_PATTERN));

    @BeforeEach
    public void setupTests() {
        userId = randomUUID();
        organisationId = randomUUID().toString();
        newOrganisationId = randomUUID().toString();
        organisationName = new SimpleStringGenerator(5, 15).next();
        newOrganisationName = new SimpleStringGenerator(5, 15).next();

        resetWiremock();
        stubForReferenceDataQueryOffence();
        stubAccessControlForAllUsers(true, userId, "Defence Lawyers", "System Users");
        stubUsersGroupsPermission();
        stubGetOrganisationDetailsForUser(userId, UUID.fromString(organisationId));
        stubGetUsersAndGroupsQueryForDefenceUsers(userId.toString());
        stubGetOrganisationNamesForIds(userId, asList(UUID.fromString(organisationId), UUID.fromString(newOrganisationId)));
        stubGetOrganisationDetails(organisationId, organisationName);
        stubGetOrganisationDetails(newOrganisationId, newOrganisationName);
        stubForProsecutionCaseQuery();
        stubUserPermissions();
        mockDefenceUserPleaAccessControl(userId.toString());

        caseId = randomUUID();
        urn = regexGenerator.next();
        firstName = new SimpleStringGenerator(5, 15).next();
        lastName = new SimpleStringGenerator(5, 15).next();
        dateOfBirth = new LocalDateGenerator(ofYears(10), parse("1983-04-20"), FUTURE).next().toString();
        defendantId = randomUUID().toString();


    }

    @Test
    public void shouldAddPlea_organisationDefendant() throws Exception {

        createProsecutionCaseHelper.createAndVerifyProsecutionCaseWithOrganisationDefendant(caseId, urn, defendantId, organisationName, userId);

        assignDefenceAssociationForUserAndDefendant(defendantId, userId);

        final String allocationId = randomUUID().toString();
        addPleaAllocationForOrganisation(defendantId, userId.toString(), allocationId);
        pollForDefencePleas(caseId.toString(), userId, List.of(
                withJsonPath("$.pleasAllocation[0].defendantId", is(defendantId)),
                withJsonPath("$.pleasAllocation[0].disputeOffenceValueDetails", is("disputeOffenceValueDetails")),
                withJsonPath("$.pleasAllocation[0].defendantNameDobConfirmation", is(false))
        ));
    }

    @Test
    public void shouldAddUpdatePlea() throws Exception {
        mockDefenceUserPleaAccessControl(userId.toString());
        final String allocationId = randomUUID().toString();
        createProsecutionCaseHelper.createAndVerifyProsecutionCaseWithDefendant(caseId, urn, defendantId, firstName, lastName, dateOfBirth, randomUUID(), randomUUID(), userId);

        assignDefenceAssociationForUserAndDefendant(defendantId, userId);

        addPleaAllocation(defendantId, userId.toString(), allocationId);
        pollForDefencePleas(caseId.toString(), userId, List.of(
                withJsonPath("$.pleasAllocation[0].defendantId", is(defendantId)),
                withJsonPath("$.pleasAllocation[0].disputeOffenceValueDetails", is("disputeOffenceValueDetails")),
                withJsonPath("$.pleasAllocation[0].defendantNameDobConfirmation", is(false))
        ));

        updatePleaAllocation(defendantId, userId.toString(), allocationId, UPDATE_PLEA_ALLOCATION_REQUEST_WITH_DEFENDANT_DETAILS_CORRECTION_TEMPLATE_NAME);
        pollForDefencePleas(caseId.toString(), userId, List.of(
                withJsonPath("$.pleasAllocation[0].defendantId", is(defendantId)),
                withJsonPath("$.pleasAllocation[0].disputeOffenceValueDetails", is("dispute-details-updated")),
                withJsonPath("$.pleasAllocation[0].defendantNameDobConfirmation", is(false))
        ));

        updatePleaAllocation(defendantId, userId.toString(), allocationId, UPDATE_PLEA_ALLOCATION_REQUEST_WITHOUT_DEFENDANT_DETAILS_CORRECTION_TEMPLATE_NAME);
        pollForDefencePleas(caseId.toString(), userId, List.of(
                withJsonPath("$.pleasAllocation[0].defendantId", is(defendantId)),
                withJsonPath("$.pleasAllocation[0].disputeOffenceValueDetails", is("dispute-details-updated")),
                withJsonPath("$.pleasAllocation[0].defendantNameDobConfirmation", is(true))
        ));
    }

    private void assignDefenceAssociationForUserAndDefendant(final String defendantId, final UUID userId) throws IOException {
        associateOrganisation(defendantId, userId.toString());
        verifyDefenceOrganisationAssociatedDataPersisted(defendantId, organisationId, userId.toString());
    }

}
