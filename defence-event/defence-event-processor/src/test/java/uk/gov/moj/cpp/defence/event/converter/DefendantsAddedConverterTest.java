package uk.gov.moj.cpp.defence.event.converter;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.defence.event.processor.TestTemplates.getDefendantDetails;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.moj.cpp.defence.json.schema.event.DefendantAdded;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefendantsAddedConverterTest {

    private static final boolean INDIVIDUAL_DEFENDANT = TRUE;
    @InjectMocks
    DefendantsAddedConverter defendantsAddedConverter;
    @Spy
    ConverterUtil converterUtil;

    @Test
    public void shouldConvertToDefendantAddedForIndividualDefendant() {

        Defendant defendant = getDefendantDetails(INDIVIDUAL_DEFENDANT).get(0);
        DefendantAdded defendantAdded = defendantsAddedConverter.convert(defendant);
        assertThat(defendantAdded.getPoliceDefendantId(), is(defendant.getProsecutionAuthorityReference()));
        assertThat(defendantAdded.getDefendantId(), is(defendant.getId()));
        assertThat(defendantAdded.getDefendantDetails().getFirstName(), is(defendant.getPersonDefendant().getPersonDetails().getFirstName()));
        assertThat(defendantAdded.getDefendantDetails().getLastName(), is(defendant.getPersonDefendant().getPersonDetails().getLastName()));
        assertThat(defendantAdded.getDefendantDetails().getDateOfBirth(), is(defendant.getPersonDefendant().getPersonDetails().getDateOfBirth()));
        assertThat(defendantAdded.getDefendantDetails().getCaseId(), is(defendant.getProsecutionCaseId()));
        assertThat(defendantAdded.getPoliceDefendantId(), is(defendant.getProsecutionAuthorityReference()));

    }

    @Test
    public void shouldConvertToDefendantAddedForCorporateDefendant() {

        Defendant defendant = getDefendantDetails(!INDIVIDUAL_DEFENDANT).get(0);
        DefendantAdded defendantAdded = defendantsAddedConverter.convert(defendant);
        assertThat(defendantAdded.getPoliceDefendantId(), is(defendant.getProsecutionAuthorityReference()));
        assertThat(defendantAdded.getDefendantId(), is(defendant.getId()));
        assertThat(defendantAdded.getDefendantDetails().getCaseId(), is(defendant.getProsecutionCaseId()));
        assertThat(defendantAdded.getDefendantDetails().getOrganisation().getOrganisationName(), is(defendant.getLegalEntityDefendant().getOrganisation().getName()));
        assertThat(defendantAdded.getDefendantDetails().getOrganisation().getAddress().getAddress1(), is(defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress1()));
        assertThat(defendantAdded.getDefendantDetails().getOrganisation().getAddress().getAddress2(), is(defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress2()));
        assertThat(defendantAdded.getDefendantDetails().getOrganisation().getAddress().getAddress3(), is(defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress3()));
        assertThat(defendantAdded.getDefendantDetails().getOrganisation().getAddress().getAddress4(), is(defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress4()));
        assertThat(defendantAdded.getDefendantDetails().getOrganisation().getAddress().getAddressPostcode(), is(defendant.getLegalEntityDefendant().getOrganisation().getAddress().getPostcode()));
        assertThat(defendantAdded.getDefendantDetails().getOrganisation().getAliasOrganisationNames().get(0), is(defendant.getAliases().get(0).getLegalEntityName()));
        assertThat(defendantAdded.getPoliceDefendantId(), is(defendant.getProsecutionAuthorityReference()));

    }


}
