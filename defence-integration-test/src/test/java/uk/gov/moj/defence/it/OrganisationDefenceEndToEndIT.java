package uk.gov.moj.defence.it;


import static java.time.LocalDate.parse;
import static java.time.Period.ofYears;
import static java.util.UUID.randomUUID;
import static java.util.regex.Pattern.compile;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.DateGenerator.Direction.FUTURE;
import static uk.gov.moj.defence.domain.common.UrnRegex.URN_PATTERN;
import static uk.gov.moj.defence.util.AccessControlStub.stubAccessControl;
import static uk.gov.moj.defence.util.MaterialQueryStub.stubForMaterialQueryPerson;
import static uk.gov.moj.defence.util.ProsecutionCaseQueryStub.stubForProsecutionCaseQuery;
import static uk.gov.moj.defence.util.ReferenceDataOffencesQueryStub.stubForReferenceDataQueryOffence;
import static uk.gov.moj.defence.util.TestUtils.getPayloadForCreatingRequest;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationDetailsForUser;
import static uk.gov.moj.defence.util.UsersGroupStub.stubGetOrganisationNamesForIds;
import static uk.gov.moj.defence.util.UsersGroupStub.stubUserPermissions;
import static uk.gov.moj.defence.util.WiremockHelper.resetWiremock;

import uk.gov.justice.json.generator.value.string.RegexGenerator;
import uk.gov.justice.json.generator.value.string.SimpleStringGenerator;
import uk.gov.justice.services.test.utils.core.random.LocalDateGenerator;
import uk.gov.moj.defence.helper.CreateProsecutionCaseHelper;
import uk.gov.moj.defence.helper.OrganisationHelper;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrganisationDefenceEndToEndIT {


    private static final String MATERIAL_ID = randomUUID().toString();
    private static boolean POLL_BY_ORGANISATION = true;
    private static final UUID userId = randomUUID();
    private static final RegexGenerator regexGenerator = new RegexGenerator(compile(URN_PATTERN));
    private static UUID organisationId;
    private static final String firstName = "";
    private static final String lastName = "";
    private static final String dob = "";

    private final SimpleStringGenerator simpleStringGenerator = new SimpleStringGenerator(5, 15);
    private String defenceClientIdString;
    private String organisationName;
    private String secondOrganisationName;
    private CreateProsecutionCaseHelper createProsecutionCaseHelper;
    private OrganisationHelper organisationHelper;
    private UUID caseId;
    private String urn;
    private String dateOfBirth;

    @BeforeAll
    public static void setupClass() {
        resetWiremock();
        stubForReferenceDataQueryOffence();
        stubForMaterialQueryPerson();
        organisationId = randomUUID();
        stubAccessControl(true, userId, "Defence Lawyers");
        stubGetOrganisationDetailsForUser(userId, organisationId);
        stubGetOrganisationNamesForIds(userId, asList(organisationId));
        stubUserPermissions();
        stubForProsecutionCaseQuery();
    }

    @BeforeEach
    public void setupTests() {
        createProsecutionCaseHelper = new CreateProsecutionCaseHelper();
        organisationHelper = new OrganisationHelper();
        caseId = randomUUID();
        urn = regexGenerator.next();
        organisationName = simpleStringGenerator.next();
        secondOrganisationName = simpleStringGenerator.next();
        defenceClientIdString = randomUUID().toString();
        dateOfBirth = new LocalDateGenerator(ofYears(10), parse("1983-04-20"), FUTURE).next().toString();
    }

    @Test
    public void testEndToEndFlowWithOrganisationDefendantAddedPublicEvent() {

        createProsecutionCaseHelper.createAndVerifyProsecutionCase(caseId, urn, "SPI", dateOfBirth, userId);
        final String generatedDefenceDefendantId = organisationHelper.addDefendant(urn, getDefendantAddedData(caseId.toString()), firstName, lastName, dob, organisationName, userId, organisationId, POLL_BY_ORGANISATION);

        //now record instruction details
        organisationHelper.recordInstructionDetails(urn, firstName, lastName, dateOfBirth, generatedDefenceDefendantId, userId, organisationId, organisationName, POLL_BY_ORGANISATION);
        organisationHelper.verifyAllegations(generatedDefenceDefendantId, false, userId);

        organisationHelper.publishIdpcEvent(caseId.toString(), defenceClientIdString, generatedDefenceDefendantId, urn, MATERIAL_ID, userId, organisationName);
        organisationHelper.downloadIDPCAndVerifyAllegationStatus(generatedDefenceDefendantId, userId, organisationId, MATERIAL_ID);
    }

    @Test
    public void testEndToEndFlowWithCCCaseReceivedEvent() {

        createProsecutionCaseHelper.createAndVerifyProsecutionCaseWithOrganisationDefendant(caseId, urn, defenceClientIdString, organisationName, userId);

        final String generatedDefenceDefendantId = getGeneratedOrganisationDefendantId(urn, organisationName);
        organisationHelper.recordInstructionDetails(urn, firstName, lastName, dob, generatedDefenceDefendantId, userId, organisationId, organisationName, POLL_BY_ORGANISATION);
        organisationHelper.verifyAllegations(generatedDefenceDefendantId, false, userId);
        organisationHelper.publishIdpcEvent(caseId.toString(), defenceClientIdString, generatedDefenceDefendantId, urn, MATERIAL_ID, userId, organisationName);
        organisationHelper.downloadIDPCAndVerifyAllegationStatus(generatedDefenceDefendantId, userId, organisationId, MATERIAL_ID);

    }

    @Test
    public void testEndToEndFlowWithCCCaseReceivedEventWithoutASN() {

        createProsecutionCaseHelper.createAndVerifyProsecutionCaseWithOrganisationDefendantNotHavingASN(caseId, urn, defenceClientIdString, organisationName, userId);

        final String generatedDefenceDefendantId = getGeneratedOrganisationDefendantId(urn, organisationName);
        organisationHelper.recordInstructionDetails(urn, firstName, lastName, dob, generatedDefenceDefendantId, userId, organisationId, organisationName, POLL_BY_ORGANISATION);
        organisationHelper.verifyAllegations(generatedDefenceDefendantId, false, userId);
        organisationHelper.publishIdpcEvent(caseId.toString(), defenceClientIdString, generatedDefenceDefendantId, urn, MATERIAL_ID, userId, organisationName);
        organisationHelper.downloadIDPCAndVerifyAllegationStatus(generatedDefenceDefendantId, userId, organisationId, MATERIAL_ID);

    }

    @Test
    public void shouldRaiseEventProsecutionCaseFileReceivedAndInvokeCommandForMultipleOrganisationDefendants() {

        final String secondDefendantOrganisationName = simpleStringGenerator.next();
        final String secondDefendantIdString = randomUUID().toString();

        createProsecutionCaseHelper.createAndVerifyProsecutionCaseWithTwoOrganisationDefendants(caseId, urn, defenceClientIdString, secondDefendantIdString, organisationName,
                secondDefendantOrganisationName, userId);

        final String generatedDefenceDefendantId = getGeneratedOrganisationDefendantId(urn, organisationName);
        getGeneratedOrganisationDefendantId(urn, secondDefendantOrganisationName);
        organisationHelper.recordInstructionDetails(urn, firstName, lastName, dob, generatedDefenceDefendantId, userId, organisationId, organisationName, POLL_BY_ORGANISATION);

        organisationHelper.verifyAllegations(generatedDefenceDefendantId, false, userId);

        assertThat(generatedDefenceDefendantId, is(notNullValue()));
        organisationHelper.publishIdpcEvent(caseId.toString(), defenceClientIdString, generatedDefenceDefendantId, urn, MATERIAL_ID, userId, organisationName);

        organisationHelper.downloadIDPCAndVerifyAllegationStatus(generatedDefenceDefendantId, userId, organisationId, MATERIAL_ID);
    }

    @Test
    public void shouldHandleEventSpiProsecutionCaseFileDefendantAdded() {

        final String secondDefendantOrganisationName = simpleStringGenerator.next();
        final String secondDefenceClientIdString = randomUUID().toString();
        createProsecutionCaseHelper.createAndVerifySPIProsecutionCaseWithOrganisationDefendantAdded(caseId, defenceClientIdString, secondDefenceClientIdString,
                organisationName, secondDefendantOrganisationName, userId);
    }

    @Test
    public void shouldRaiseDefenceClientReceivedEventAndPersistDefenceClientDataWhenCcCaseReceivedEventOccurred() {
        final String defendantId = randomUUID().toString();
        createProsecutionCaseHelper.createAndVerifyProsecutionCaseWithOrganisationDefendant(caseId, urn, defendantId, organisationName, userId);
        organisationHelper.verifyDefenceClient(caseId, urn, firstName, lastName, dob, defendantId, userId, organisationName, POLL_BY_ORGANISATION);
    }


    private String getDefendantAddedData(final String caseId) {
        String defendantAddedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.organisation-defendants-added-to-court-proceedings.json");
        defendantAddedData = defendantAddedData.replace("CASE-ID", caseId);
        defendantAddedData = defendantAddedData.replace("DEFENDANT-ID", defenceClientIdString);
        defendantAddedData = defendantAddedData.replace("ORGANISATION-NAME", organisationName);
        defendantAddedData = defendantAddedData.replace("ORGANISATION-NAME-1", secondOrganisationName);
        return defendantAddedData;

    }

    private String getGeneratedOrganisationDefendantId(final String urn, final String organisationName) {
        return organisationHelper.getGeneratedDefendantId(urn, firstName, lastName, dob, organisationName, userId, POLL_BY_ORGANISATION);
    }

}
