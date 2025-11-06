package uk.gov.moj.cpp.defence.aggregate;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.cps.defence.DefendantDetails.defendantDetails;
import static uk.gov.justice.cps.defence.DefendantOffender.defendantOffender;
import static uk.gov.justice.cps.defence.Offence.offence;
import static uk.gov.justice.cps.defence.OffenceCode.offenceCode;

import uk.gov.justice.cps.defence.Cpr;
import uk.gov.justice.cps.defence.DefenceClientDetails;
import uk.gov.justice.cps.defence.DefenceClientMappedToACase;
import uk.gov.justice.cps.defence.DefendantAdded;
import uk.gov.justice.cps.defence.DefendantDetails;
import uk.gov.justice.cps.defence.DuplicateDefendantReceivedAgainstADefenceClient;
import uk.gov.justice.cps.defence.Offence;
import uk.gov.moj.cpp.defence.events.ProsecutionCaseReceived;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CaseDefenceClientMapTest {

    private static final UUID CASE_ID = randomUUID();
    private static final UUID DEFENCE_CLIENT_ID1 = randomUUID();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final String PROSECUTION_AUTHORITY = "TFL";
    private static final String URN = "55DP0028116";
    private static final String POLICE_DEFENDANT_ID = "BS2CM01ADEF1";
    private static final String FIRST_NAME = "BS2CM01ADEF1";
    private static final String LAST_NAME = "BS2CM01ADEF1";
    private static final String DOB = "2010-10-10";
    private static final boolean IS_CIVIL = true;
    private static final boolean IS_GROUP_MEMBER = true;
    private static final DefendantDetails DEFENCE_CLIENT_IDS = defendantDetails()
            .withCaseId(CASE_ID)
            .withId(DEFENCE_CLIENT_ID1)
            .withFirstName(FIRST_NAME)
            .withLastName(LAST_NAME)
            .withDateOfBirth(DOB)
            .build();

    private CaseDefenceClientMap caseDefenceClientMap;

    @BeforeEach
    public void setup() {
        caseDefenceClientMap = new CaseDefenceClientMap();
    }

    @Test
    public void shouldHandleCaseCreatedAfterSuspectAdded() {

        final Stream<Object> eventStream = caseDefenceClientMap.receiveDetails(CASE_ID, URN, PROSECUTION_AUTHORITY, IS_CIVIL, IS_GROUP_MEMBER);

        final List<?> eventList = eventStream.collect(Collectors.toList());

        assertThat("Unexpected number of events received!", eventList.size(), is(1));
        assertThat("Unexpected event type!", eventList.get(0).getClass().getName(), is(ProsecutionCaseReceived.class.getName()));
        final ProsecutionCaseReceived prosecutionCaseReceived = (ProsecutionCaseReceived) eventList.get(0);
        assertThat(prosecutionCaseReceived.getIsCivil(), is(IS_CIVIL));
        assertThat(prosecutionCaseReceived.getIsGroupMember(), is(IS_GROUP_MEMBER));
    }

    @Test
    public void shouldReceiveCivilProsecutionCase() {
        final Stream<Object> eventStream = caseDefenceClientMap.receiveDetails(CASE_ID, URN, PROSECUTION_AUTHORITY, IS_CIVIL, IS_GROUP_MEMBER);
        final List<?> eventList = eventStream.collect(Collectors.toList());
        final ProsecutionCaseReceived prosecutionCaseReceived = (ProsecutionCaseReceived) eventList.get(0);

        assertThat(prosecutionCaseReceived.getIsCivil(), is(IS_CIVIL));
        assertThat(prosecutionCaseReceived.getIsGroupMember(), is(IS_GROUP_MEMBER));
        assertThat(prosecutionCaseReceived.getCaseId(), is(CASE_ID));
        assertThat(prosecutionCaseReceived.getUrn(), is(URN));
        assertThat(prosecutionCaseReceived.getProsecutingAuthority(), is(PROSECUTION_AUTHORITY));
    }

    @Test
    public void shouldHandleDuplicateDefendantAddedAfterSupspectCharged() throws Exception {

        final List<Offence> offenceList = getTestOffenceList();

        caseDefenceClientMap.addADefendant(DEFENDANT_ID, DEFENCE_CLIENT_IDS, POLICE_DEFENDANT_ID, offenceList);

        final Stream<Object> duplicateDefendantReceivedEventStreams = caseDefenceClientMap.addADefendant(DEFENDANT_ID, DEFENCE_CLIENT_IDS, POLICE_DEFENDANT_ID, offenceList);

        final DuplicateDefendantReceivedAgainstADefenceClient duplicateDefendantReceivedAgainstADefenceClient
                = duplicateDefendantReceivedEventStreams.filter(event -> event.getClass().equals(DuplicateDefendantReceivedAgainstADefenceClient.class))
                .map(e -> ((DuplicateDefendantReceivedAgainstADefenceClient) e))
                .findFirst()
                .orElseThrow(() -> new Exception("Expected DuplicateDefendantReceivedAgainstADefenceClient event!"));

        assertThat(duplicateDefendantReceivedAgainstADefenceClient.getDuplicateDefendantId(), is(DEFENDANT_ID));
    }

    @Test
    public void shouldHandleSuspectChargedBeforeSuspectAddedToCase() {

        caseDefenceClientMap.receiveDetails(CASE_ID, URN, PROSECUTION_AUTHORITY, !IS_CIVIL, !IS_GROUP_MEMBER);

        final List<Offence> offenceList = getTestOffenceList();

        final Stream<Object> eventStream = caseDefenceClientMap.addADefendant(DEFENDANT_ID, DEFENCE_CLIENT_IDS, POLICE_DEFENDANT_ID, offenceList);

        final List<?> eventList = eventStream.collect(Collectors.toList());

        assertThat(eventList.size(),is(2));
        assertThat(eventList.get(0).getClass().getName(), is(DefenceClientMappedToACase.class.getName()));
        assertThat(eventList.get(1).getClass().getName(), is(DefendantAdded.class.getName()));

        final DefenceClientMappedToACase defenceClientMappedToACaseEvent = (DefenceClientMappedToACase) eventList.get(0);
        assertThat(defenceClientMappedToACaseEvent.getDefenceClientId(), notNullValue());
        assertThat(reflectionEquals(DEFENCE_CLIENT_IDS, defenceClientMappedToACaseEvent.getDefendantDetails()), is(true));
        assertThat(reflectionEquals(DefenceClientDetails.defenceClientDetails().build(), defenceClientMappedToACaseEvent.getDefenceClientDetails()), is(true));
        assertThat(defenceClientMappedToACaseEvent.getUrn(), is(URN));

        final DefendantAdded defendantAddedEvent = (DefendantAdded) eventList.get(1);
        assertThat(defenceClientMappedToACaseEvent.getDefenceClientId(), is(defendantAddedEvent.getDefenceClientId()));
        assertThat(reflectionEquals(DEFENCE_CLIENT_IDS, defendantAddedEvent.getDefendantDetails()), is(true));
        assertThat(defendantAddedEvent.getDefendantId(), is(DEFENDANT_ID));
        assertThat(defendantAddedEvent.getOffences(),notNullValue());
        assertThat(defendantAddedEvent.getPoliceDefendantId(), is(POLICE_DEFENDANT_ID));

        final Offence offenceFromEventOne = defendantAddedEvent.getOffences().get(0);
        final Offence offenceFromEventTwo = defendantAddedEvent.getOffences().get(1);

        assertThat(reflectionEquals(offenceList.get(0), offenceFromEventOne, "cpr", "offenceCodeDetails"),is(true));
        assertThat(reflectionEquals(offenceList.get(1), offenceFromEventTwo, "cpr", "offenceCodeDetails"),is(true));

        assertThat(reflectionEquals(offenceList.get(0).getCpr(), offenceFromEventOne.getCpr(), "defendantOffender"),is(true));
        assertThat(reflectionEquals(offenceList.get(1).getCpr(), offenceFromEventTwo.getCpr(), "defendantOffender"),is(true));
        assertThat(reflectionEquals(offenceList.get(0).getCpr().getDefendantOffender(), offenceFromEventOne.getCpr().getDefendantOffender()),is(true));
        assertThat(reflectionEquals(offenceList.get(1).getCpr().getDefendantOffender(), offenceFromEventTwo.getCpr().getDefendantOffender()),is(true));
        assertThat(reflectionEquals(offenceList.get(0).getOffenceCodeDetails(), offenceFromEventOne.getOffenceCodeDetails()),is(true));
        assertThat(reflectionEquals(offenceList.get(1).getOffenceCodeDetails(), offenceFromEventTwo.getOffenceCodeDetails()),is(true));
    }

    private List<Offence> getTestOffenceList() {

        final List<Offence> offenceList = new ArrayList<>();

        offenceList.add(offence()
                .withId(randomUUID())
                .withPoliceOffenceId("POLICE OFFENCE ID 1")
                .withCpr(Cpr.cpr()
                        .withDefendantOffender(defendantOffender()
                                .withYear("09")
                                .withOrganisationUnit("ORG UNIT 1")
                                .withNumber("NUMBER 1")
                                .withCheckDigit("A")
                                .build())
                        .withCjsCode("CJS CODE 1")
                        .withOffenceSequence("001")
                        .build())
                .withAsnSequenceNumber("001")
                .withCjsCode("CJS OFFENCE CODE 1")
                .withOffenceCodeDetails(offenceCode()
                        .withCjsoffencecode("CJS OFFENCE CODE 1")
                        .withCustodialIndicatorCode("CI CODE 1")
                        .withDateCreated("2018-02-01")
                        .withDateOfLastUpdate("2018-02-01")
                        .withId("ID 1")
                        .withLegislation("LEGISLATION 1")
                        .withLibraCategoryCode("LIBRA CATEGORY CODE 1")
                        .withMaxfinetypecrownct("MAX FINE TYPE CROWN CT 1")
                        .withMaxfinetypemagct("MAX FINE TYPE MAG CT 1")
                        .withMisCode("MIS CODE 1")
                        .withModeoftrial("MODE OF TRIAL 1")
                        .withModeoftrialdescription("MODE OF TRIAL DESCRIPTION 1")
                        .withOffenceenddate("2018-02-01")
                        .withOffencestartdate("2018-02-01")
                        .withPnldref("PNLD REF 1")
                        .withPoliceandcpschargingresponsibilities("PACR 1")
                        .withStandardoffencewording("STANDARD OFFENCE WORDING 1")
                        .withStandardstatementoffacts("STANDARD STATEMENT OF FACTS 1")
                        .withTimelimitforprosecutions("TIME LIMIT FOR PROSECUTIONS 1")
                        .withTitle("TITLE 1")
                        .withWelshLegislation("WELSH LEGISLATION 1")
                        .withWelshOffenceTitle("WELSH OFFENCE TITLE 1")
                        .withWelshstandardoffencewording("WELSH STANDARD OFFENCE WORDING 1")
                        .withWelshstandardstatementoffacts("WELSH STANDARD STATEMENT OF FACTS 1")
                        .build()
                )
                .withReason("REASON 1")
                .withDescription("DESCRIPTION 1")
                .withWording("WORDING 1")
                .withCategory("CATEGORY 1")
                .withArrestDate("2018-02-15")
                .withStartDate("2018-02-15")
                .withEndDate("2018-02-15")
                .withChargeDate("2018-02-16")
                .build());

        offenceList.add(offence()
                .withId(randomUUID())
                .withPoliceOffenceId("POLICE OFFENCE ID 2")
                .withCpr(Cpr.cpr()
                        .withDefendantOffender(defendantOffender()
                                .withYear("09")
                                .withOrganisationUnit("ORG UNIT 2")
                                .withNumber("NUMBER 2")
                                .withCheckDigit("A")
                                .build())
                        .withCjsCode("CJS CODE 2")
                        .withOffenceSequence("002")
                        .build())
                .withAsnSequenceNumber("002")
                .withCjsCode("CJS OFFENCE CODE 2")
                .withOffenceCodeDetails(offenceCode()
                        .withCjsoffencecode("CJS OFFENCE CODE 2")
                        .withCustodialIndicatorCode("CI CODE 2")
                        .withDateCreated("2018-02-01")
                        .withDateOfLastUpdate("2018-02-01")
                        .withId("ID 2")
                        .withLegislation("LEGISLATION 2")
                        .withLibraCategoryCode("LIBRA CATEGORY CODE 2")
                        .withMaxfinetypecrownct("MAX FINE TYPE CROWN CT 2")
                        .withMaxfinetypemagct("MAX FINE TYPE MAG CT 2")
                        .withMisCode("MIS CODE 2")
                        .withModeoftrial("MODE OF TRIAL 2")
                        .withModeoftrialdescription("MODE OF TRIAL DESCRIPTION 2")
                        .withOffenceenddate("2018-02-01")
                        .withOffencestartdate("2018-02-01")
                        .withPnldref("PNLD REF 2")
                        .withPoliceandcpschargingresponsibilities("PACR 2")
                        .withStandardoffencewording("STANDARD OFFENCE WORDING 2")
                        .withStandardstatementoffacts("STANDARD STATEMENT OF FACTS 2")
                        .withTimelimitforprosecutions("TIME LIMIT FOR PROSECUTIONS 2")
                        .withTitle("TITLE 2")
                        .withWelshLegislation("WELSH LEGISLATION 2")
                        .withWelshOffenceTitle("WELSH OFFENCE TITLE 2")
                        .withWelshstandardoffencewording("WELSH STANDARD OFFENCE WORDING 2")
                        .withWelshstandardstatementoffacts("WELSH STANDARD STATEMENT OF FACTS 2")
                        .build()
                )
                .withReason("REASON 2")
                .withDescription("DESCRIPTION 2")
                .withWording("WORDING 2")
                .withCategory("CATEGORY 2")
                .withArrestDate("2018-02-14")
                .withStartDate("2018-02-14")
                .withEndDate("2018-02-14")
                .withChargeDate("2018-02-16")
                .build());

        return offenceList;
    }
}
