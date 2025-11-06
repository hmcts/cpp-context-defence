package uk.gov.moj.cpp.defence.event.processor;

import static com.google.common.collect.ImmutableList.of;
import static java.lang.Boolean.TRUE;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.DefendantAlias;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.cps.defence.Offence;
import uk.gov.moj.cpp.defence.CaseDetails;
import uk.gov.moj.cpp.defence.Defendant;
import uk.gov.moj.cpp.defence.event.processor.events.CaseRemovedFromGroupCases;
import uk.gov.moj.cpp.progression.json.schema.event.ProsecutionCaseCreated;
import uk.gov.moj.cpp.prosecutioncasefile.json.schema.event.Channel;
import uk.gov.moj.cpp.prosecutioncasefile.json.schema.event.PublicCcCaseReceived;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestTemplates {

    private static final UUID DEFAULT_DEFENDANT_ID1 = fromString("dddd1111-1e20-4c21-916a-81a6c90239e5");
    private static final UUID DEFAULT_DEFENDANT_ID2 = fromString("dddd2222-1e20-4c21-916a-81a6c90239e5");

    private TestTemplates() {

    }

    public static PublicCcCaseReceived basicCCCaseReceivedTemplate() {
        final List<Defendant> defendantList = getDefendants();


        return PublicCcCaseReceived.publicCcCaseReceived()
                .withCaseDetails(buildBasicCaseDetailsTemplate())
                .withDefendants(defendantList)
                .withChannel(Channel.SPI)
                .build();
    }

    public static List<Defendant> getDefendants() {
        final List<Defendant> defendantList = new ArrayList<>();
        defendantList.add(basicDefendantWithOffenceTemplate(DEFAULT_DEFENDANT_ID1, UUID.fromString("b30a78e1-d5f5-4c35-a406-29a42fe0b3c0")));
        defendantList.add(basicDefendantWithOffenceTemplate(DEFAULT_DEFENDANT_ID2, UUID.fromString("a1e00fd9-3a27-43da-97d8-6be65044e2fc")));
        return defendantList;
    }

    private static CaseDetails buildBasicCaseDetailsTemplate() {
        return CaseDetails.caseDetails().withCaseId(randomUUID()).withProsecutorCaseReference("caseRef").build();
    }

    private static Defendant basicDefendantWithOffenceTemplate(final UUID defendantId, final UUID offenceId) {

        return Defendant.defendant()
                .withId(defendantId.toString())
                .withFirstName("John")
                .withLastName("Smith")
                .withDateOfBirth(LocalDate.of(2000, 11, 11))
                .withProsecutorDefendantReference("TVL12345")
                .withAsn("asnValue")
                .withOffences(of(
                        Offence.offence()
                                .withId(offenceId)
                                .withPoliceOffenceId("12345")
                                .withAsnSequenceNumber("001")
                                .withCjsCode("cjsCode")
                                .withDescription("desc")
                                .withCategory("category")
                                .withStartDate("2019-01-01")
                                .withWording("Malicious Intent to Crime etc").build(),
                        Offence.offence()
                                .withId(offenceId)
                                .withPoliceOffenceId("12345")
                                .withAsnSequenceNumber("002")
                                .withCjsCode("cjsCode")
                                .withDescription("desc")
                                .withWording("Malicious Intent to Robbery ")
                                .withCategory("category")
                                .withStartDate("2019-01-01")
                                .build()))
                .build();
    }


    public static ProsecutionCaseCreated createProsecutionCaseCreatedEvent(boolean isSjp, boolean isCivil, boolean isGroupMember) {

        ProsecutionCaseIdentifier.Builder builder = ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                .withProsecutionAuthorityCode("AUTH")
                .withProsecutionAuthorityId(randomUUID());
        if (isSjp) {
            builder.withCaseURN("REF123");
        } else {
            builder.withProsecutionAuthorityReference("REF123");
        }

        return ProsecutionCaseCreated.prosecutionCaseCreated().
                withProsecutionCase(
                        ProsecutionCase.prosecutionCase()
                                .withId(randomUUID())
                                .withIsCivil(isCivil)
                                .withIsGroupMember(isGroupMember)
                                .withDefendants(getDefendantDetails(TRUE))
                                .withProsecutionCaseIdentifier(builder.build())
                                .build())
                .build();
    }

    public static CaseRemovedFromGroupCases createCaseRemovedFromGroupCasesEvent() {

        return CaseRemovedFromGroupCases.caseRemovedFromGroupCases().
                withRemovedCase(
                        ProsecutionCase.prosecutionCase()
                                .withId(randomUUID())
                                .withDefendants(getDefendantDetails(TRUE))
                                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                                        .withProsecutionAuthorityCode("AUTH")
                                        .withProsecutionAuthorityId(randomUUID())
                                        .withCaseURN("REMOVED_CASE_URN")
                                        .build())
                                .build())
                .build();
    }

    public static List<uk.gov.justice.core.courts.Defendant> getDefendantDetails(boolean isIndividualDefendant) {
        final List<uk.gov.justice.core.courts.Defendant> defendants = new ArrayList<>();

        uk.gov.justice.core.courts.Defendant.Builder builder =
                uk.gov.justice.core.courts.Defendant.defendant()
                        .withId(randomUUID())
                        .withProsecutionCaseId(randomUUID())
                        .withAliases(of(DefendantAlias.defendantAlias()
                                .withLegalEntityName("Alias0")
                                .build())
                        );
        if (isIndividualDefendant) {
            builder.withPersonDefendant(PersonDefendant.personDefendant()
                    .withPersonDetails(Person.person()
                            .withFirstName("first")
                            .withLastName("last")
                            .withDateOfBirth("1990-11-11")
                            .withTitle("MR")
                            .build())
                    .build());
        } else {
            builder.withLegalEntityDefendant(LegalEntityDefendant.legalEntityDefendant()
                    .withOrganisation(Organisation.organisation()
                            .withAddress(Address.address()
                                    .withAddress1("ADDRESS1")
                                    .build())
                            .withContact(ContactNumber.contactNumber()
                                    .withWork("12345678")
                                    .build())
                            .build())
                    .build());
        }
        builder.withOffences(of(
                uk.gov.justice.core.courts.Offence.offence()
                        .withId(randomUUID())
                        .withStartDate("2019-01-01")
                        .withArrestDate("11-07-2020")
                        .withChargeDate("11-07-2020")
                        .withOffenceDefinitionId(randomUUID())
                        .withWording("Malicious Intent to Crime etc").build()));
        defendants.add(builder.build());
        return defendants;
    }

}
