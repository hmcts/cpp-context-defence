package uk.gov.moj.defence.helper;

import static java.time.LocalDate.parse;
import static java.time.Period.ofYears;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.DateGenerator.Direction.FUTURE;
import static uk.gov.moj.defence.util.DBUtils.selectDefenceClient;
import static uk.gov.moj.defence.util.RestQueryUtil.pollCaseByOrganisationDefendant;
import static uk.gov.moj.defence.util.RestQueryUtil.pollCaseByPersonDefendant;
import static uk.gov.moj.defence.util.TestUtils.getPayloadForCreatingRequest;
import static uk.gov.moj.defence.util.TestUtils.postMessageToTopic;
import static uk.gov.moj.defence.util.TestUtils.postMessageToTopicAndVerify;
import static uk.gov.moj.defence.util.TestUtils.postPublicMessageToTopic;
import static uk.gov.moj.defence.util.TestUtils.waitForDefenceClientToBeUpdated;

import uk.gov.justice.services.test.utils.core.random.LocalDateGenerator;
import uk.gov.moj.cpp.defence.persistence.entity.DefenceClient;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class CreateProsecutionCaseHelper {

    public static final String PROSECUTING_AUTHORITY_TFL = "TFL";
    private static final String DEFENDANT_ID = "DEFENDANT_ID";
    private static final String ORGANISATION_ID = "ORGANISATION_ID";
    private static final String CASE_ID = "CASE_ID";
    private static final String URN = "URN_VALUE";
    private static final String ORGANISATION_1_NAME = "ORGANISATION_1_NAME";
    public static final String FIRST_NAME = "FIRST_NAME";
    public static final String LAST_NAME = "LAST_NAME";
    private static final String DOB = "DOB";
    private static final String OFFENCE_ID1 = "OFFENCE_ID1";
    private static final String OFFENCE_ID2 = "OFFENCE_ID2";
    private static final String PROSECUTING_AUTHORITY = "PROSECUTING_AUTHORITY";
    private static final String ORGANISATION_2_NAME = "ORGANISATION_2_NAME";
    private static final String DEF_ID2 = "DEF_ID2";
    private static final String FN_2 = "FN_2";
    private static final String LN_2 = "LN_2";
    private static final String D_O_B = "D-O-B";
    public static final String DATE_OF_BIRTH = new LocalDateGenerator(ofYears(10), parse("1983-04-20"), FUTURE).next().toString();
    private String caseId;

    public void createAndVerifyProsecutionCase(final UUID caseId, final String urn, final String channel, final String defendantId, String dateOfBirth, final UUID userId) {
        this.caseId = caseId.toString();
        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.prosecution-case-created.json")
                .replace("CASE_ID", caseId.toString())
                .replace(URN, urn)
                .replace(DOB, dateOfBirth)
                .replace(DEFENDANT_ID, defendantId)
                .replace(PROSECUTING_AUTHORITY, PROSECUTING_AUTHORITY_TFL)
                .replace(OFFENCE_ID1, randomUUID().toString())
                .replace(OFFENCE_ID2, randomUUID().toString())
                .replace("CHANNEL-NAME", channel);

        postMessageToTopic(caseCreatedData, "public.progression.prosecution-case-created");
        pollCaseByPersonDefendant(FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, userId);
    }

    public void createAndVerifyProsecutionCase(final UUID caseId, final String urn, final String channel, final String dateOfBirth, final UUID userId) {
        createAndVerifyProsecutionCase(caseId, urn, channel, randomUUID().toString(), dateOfBirth, userId);
    }

    public void createAndVerifyProsecutionCaseWithOrganisationDefendant(final UUID caseId, final String urn, final String defendantId, final String organisationName, final UUID userId) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.prosecution-case-created-with-organisation-defendant.json")
                .replace("CASE_ID", caseId.toString())
                .replace(URN, urn)
                .replace(DEFENDANT_ID, defendantId)
                .replace(PROSECUTING_AUTHORITY, PROSECUTING_AUTHORITY_TFL)
                .replace("ORGANISATION_1_NAME", organisationName);

        postMessageToTopic(caseCreatedData, "public.progression.prosecution-case-created");
        pollCaseByOrganisationDefendant(organisationName, userId);
    }

    public void createAndVerifyProsecutionCaseWithOrganisationDefendantForCivil(final UUID caseId, final String urn, final String defendantId, final String organisationName, final UUID userId) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.prosecution-case-created-with-organisation-defendant-civils.json")
                .replace("CASE_ID", caseId.toString())
                .replace(URN, urn)
                .replace(DEFENDANT_ID, defendantId)
                .replace(PROSECUTING_AUTHORITY, PROSECUTING_AUTHORITY_TFL)
                .replace("ORGANISATION_1_NAME", organisationName);

        postMessageToTopic(caseCreatedData,  "public.progression.prosecution-case-created");
        pollCaseByOrganisationDefendant(organisationName, userId);
    }

    public void createAndVerifyProsecutionCaseWithDefendant(final UUID caseId, final String urn, final String defendantId, final String firstName, final String lastName, final String dateOfBirth, UUID offenceId1, UUID offenceId2, final UUID userId) {
        this.caseId = caseId.toString();
        if (offenceId1 == null) {
            offenceId1 = randomUUID();
        }

        if (offenceId2 == null) {
            offenceId2 = randomUUID();
        }

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.prosecution-case-created.json")
                .replace(CASE_ID, caseId.toString())
                .replace("URN_VALUE", urn)
                .replace(DEFENDANT_ID, defendantId)
                .replace(FIRST_NAME, firstName)
                .replace(LAST_NAME, lastName)
                .replace(DOB, dateOfBirth)
                .replace(OFFENCE_ID1, offenceId1.toString())
                .replace(OFFENCE_ID2, offenceId2.toString())
                .replace(PROSECUTING_AUTHORITY, PROSECUTING_AUTHORITY_TFL);

        postMessageToTopic(caseCreatedData, "public.progression.prosecution-case-created");
        pollCaseByPersonDefendant(firstName, lastName, dateOfBirth, userId);
    }

    public void createAndVerifyCivilProsecutionCase(final UUID caseId, final String urn, final String defendantId, final String firstName, final String lastName, final String dateOfBirth, final UUID userId) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.prosecution-case-created-civils.json")
                .replace(CASE_ID, caseId.toString())
                .replace("URN_VALUE", urn)
                .replace(DEFENDANT_ID, defendantId)
                .replace(DOB, dateOfBirth)
                .replace(FIRST_NAME, firstName)
                .replace(LAST_NAME, lastName)
                .replace(DOB, dateOfBirth)
                .replace(OFFENCE_ID1, randomUUID().toString())
                .replace(OFFENCE_ID2, randomUUID().toString())
                .replace(PROSECUTING_AUTHORITY, PROSECUTING_AUTHORITY_TFL);

        postMessageToTopic(caseCreatedData, "public.progression.prosecution-case-created");
        pollCaseByPersonDefendant(firstName, lastName, dateOfBirth, userId);
    }

    public void createAndVerifyCivilProsecutionCaseWithoutDob(final UUID caseId, final String urn, final String defendantId, final String firstName, final String lastName) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.prosecution-case-created-civils-no-dob.json")
                .replace(CASE_ID, caseId.toString())
                .replace("URN_VALUE", urn)
                .replace(DEFENDANT_ID, defendantId)
                .replace(FIRST_NAME, firstName)
                .replace(LAST_NAME, lastName)
                .replace(OFFENCE_ID1, randomUUID().toString())
                .replace(OFFENCE_ID2, randomUUID().toString())
                .replace(PROSECUTING_AUTHORITY, PROSECUTING_AUTHORITY_TFL);

        postPublicMessageToTopic(caseCreatedData, "public.progression.prosecution-case-created");

    }


    public void createDefenceAssociationForLAA(final String defendantId, final String organisationId) {
        this.caseId = caseId;

        final String defenceAssociationForLAA = getPayloadForCreatingRequest("stub-data/public-events/public.progression.associate.defence-organisation-for-laa.json")
                .replace(DEFENDANT_ID, defendantId)
                .replace(ORGANISATION_ID, organisationId);

        postMessageToTopic(defenceAssociationForLAA, "public.progression.defence-organisation-for-laa-associated");
    }


    public void createDefenceAssociationWithOrganisationIdForLAA(final String defendantId, final String organisationId) {

        final String defenceAssociationForLAA = getPayloadForCreatingRequest("stub-data/public-events/public.progression.associate.defence-organisation-with-org-id-for-laa.json")
                .replace(DEFENDANT_ID, defendantId)
                .replace(ORGANISATION_ID, organisationId);

        postMessageToTopic(defenceAssociationForLAA, "public.progression.defence-organisation-for-laa-associated");
    }

    public void createDefenceDissociationForLAA(final String caseId, final String defendantId, final String organisationId) {
        this.caseId = caseId;

        final String defenceAssociationForLAA = getPayloadForCreatingRequest("stub-data/public-events/public.progression.associate.defence-organisation-for-laa.json")
                .replace(DEFENDANT_ID, defendantId)
                .replace(ORGANISATION_ID, organisationId)
                .replace(CASE_ID, caseId);

        postMessageToTopic(defenceAssociationForLAA, "public.progression.defence-organisation-for-laa-disassociated");
    }

    public void createLAAContractAssociationLock(final String defendantId) {

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.defendant-laa-contract-associated.json")
                .replace(DEFENDANT_ID, defendantId);

        postMessageToTopic(caseCreatedData, "public.progression.defendant-laa-contract-associated");
    }

    public void createAndVerifyProsecutionCaseWithDefendantNotHavingASN(final UUID caseId, final String urn, final String defendantId, final String firstName, final String lastName, final String dateOfBirth, final UUID userId) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.prosecution-case-created-without-asn.json")
                .replace(CASE_ID, caseId.toString())
                .replace(URN, urn)
                .replace(DEFENDANT_ID, defendantId)
                .replace(FIRST_NAME, firstName)
                .replace(LAST_NAME, lastName)
                .replace(DOB, dateOfBirth);

        postMessageToTopic(caseCreatedData, "public.progression.prosecution-case-created");
        pollCaseByPersonDefendant(firstName, lastName, dateOfBirth, userId);
    }

    public void createAndVerifyProsecutionCaseWithOrganisationDefendantNotHavingASN(final UUID caseId, final String urn, final String defendantId, final String organisationName, final UUID userId) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.prosecution-case-created-with-organisation-defendant-without-asn.json")
                .replace(CASE_ID, caseId.toString())
                .replace(URN, urn)
                .replace(DEFENDANT_ID, defendantId)
                .replace(ORGANISATION_1_NAME, organisationName);


        postMessageToTopic(caseCreatedData, "public.progression.prosecution-case-created");
        pollCaseByOrganisationDefendant(organisationName, userId);
    }

    public void createAndVerifyProsecutionCaseWithDefendantWithTwoOffences(final UUID caseId, final String urn, final String defendantId, final String firstName, final String lastName, final String dateOfBirth, final UUID userId) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.prosecution-case-created-with-defendant.json")
                .replace(CASE_ID, caseId.toString())
                .replace(URN, urn)
                .replace(DEFENDANT_ID, defendantId)
                .replace(FIRST_NAME, firstName)
                .replace(LAST_NAME, lastName)
                .replace(DOB, dateOfBirth)
                .replace(OFFENCE_ID1, randomUUID().toString())
                .replace(OFFENCE_ID2, randomUUID().toString())
                .replace(PROSECUTING_AUTHORITY, PROSECUTING_AUTHORITY_TFL);


        postMessageToTopic(caseCreatedData, "public.progression.prosecution-case-created");
        pollCaseByPersonDefendant(firstName, lastName, dateOfBirth, userId);

    }

    public void createAndVerifyProsecutionCaseWithTwoOrganisationDefendants(final UUID caseId, final String urn, final String defendantId, final String secondDefendantId,
                                                                            final String organisationName1, final String organisationName2, final UUID userId) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.prosecution-case-created-with-two-organisation-defendants.json")
                .replace(CASE_ID, caseId.toString())
                .replace(URN, urn)
                .replace(DEFENDANT_ID, defendantId)
                .replace(ORGANISATION_1_NAME, organisationName1)
                .replace(ORGANISATION_2_NAME, organisationName2)
                .replace(DEF_ID2, secondDefendantId);

        postMessageToTopic(caseCreatedData, "public.progression.prosecution-case-created");

        pollCaseByOrganisationDefendant(organisationName1, userId);
        pollCaseByOrganisationDefendant(organisationName2, userId);
    }

    public void createAndVerifyProsecutionCaseWithTwoDefendants(final UUID caseId, final String urn, final String defendantId, final String secondDefendantId,
                                                                final String firstName, final String lastName, final LocalDate dob,
                                                                final String firstName2, final String lastName2, final LocalDate dob2, final UUID userId) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.prosecution-case-created-with-two-defendants.json")
                .replace("CASE_ID", caseId.toString())
                .replace(URN, urn)
                .replace("DEFENDANT_ID", defendantId)
                .replace("FIRST_NAME", firstName)
                .replace("LAST_NAME", lastName)
                .replace("DOB", dob.toString())
                .replace("DEF_ID2", secondDefendantId)
                .replace("FN_2", firstName2)
                .replace("LN_2", lastName2)
                .replace("D-O-B", dob2.toString());

        postMessageToTopic(caseCreatedData, "public.progression.prosecution-case-created");

        pollCaseByPersonDefendant(firstName, lastName, dob.toString(), userId);
        pollCaseByPersonDefendant(firstName2, lastName2, dob2.toString(), userId);

    }


    public void createAndVerifySPIProsecutionCaseWithDefendantAddedWithNullFirstName(final UUID caseId, final String defendantId,
                                                                                     final String lastName) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.defendants-added-to-case-with-null-first-name.json")
                .replace("CASE-ID", caseId.toString())
                .replace("DEFENDANT-ID", defendantId)
                .replace("LAST-NAME", lastName);

        postMessageToTopic(caseCreatedData, "public.progression.defendants-added-to-case");

        // not sure what the equivalent functionality is on the query side
        waitForDefenceClientToBeUpdated(fromString(defendantId));
        final Optional<DefenceClient> defenceClient = selectDefenceClient(fromString(defendantId));
        assertThat(defenceClient.isPresent(), is(true));
        assertThat(defenceClient.get().getCaseId().toString(), is(this.caseId));
        assertThat(defenceClient.get().getLastName(), is(lastName));
    }

    public void createAndVerifySPIProsecutionCaseWithDefendantAdded(final UUID caseId, final String defendantId, final String secondDefendantId,
                                                                    final String firstName, final String lastName, final LocalDate dob,
                                                                    final String firstName2, final String lastName2, final LocalDate dob2, final UUID userId) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.defendants-added-to-case-with-offense.json")
                .replace("CASE-ID", caseId.toString())
                .replace("DEFENDANT-ID", defendantId)
                .replace("FIRST-NAME", firstName)
                .replace("LAST-NAME", lastName)
                .replace("DOB", dob.toString())
                .replace("DEF_ID2", secondDefendantId)
                .replace("FN-2", firstName2)
                .replace("LN-2", lastName2)
                .replace("D-O-B", dob2.toString());

        postMessageToTopic(caseCreatedData, "public.progression.defendants-added-to-case");
        pollCaseByPersonDefendant(firstName, lastName, dob.toString(), userId);
        pollCaseByPersonDefendant(firstName, lastName, dob.toString(), userId);
    }

    public void createAndVerifySPIProsecutionCaseWithOrganisationDefendantAdded(final UUID caseId, final String defendantId, final String secondDefendantId,
                                                                                final String organisationName1, final String organisationName2, final UUID userId) {
        this.caseId = caseId.toString();

        final String caseCreatedData = getPayloadForCreatingRequest("stub-data/public-events/public.progression.organisation-defendants-added-to-court-proceedings-with-offense.json")
                .replace("CASE-ID", caseId.toString())
                .replace("DEFENDANT-ID", defendantId)
                .replace("ORGANISATION-1-NAME", organisationName1)
                .replace("ORGANISATION-2-NAME", organisationName2)
                .replace("DEF_ID2", secondDefendantId);


        postMessageToTopicAndVerify(caseCreatedData, "defence.event.allegations-received-against-a-defence-client", "public.progression.defendants-added-to-case");
        pollCaseByOrganisationDefendant(organisationName1, userId);
        pollCaseByOrganisationDefendant(organisationName2, userId);
    }
}
