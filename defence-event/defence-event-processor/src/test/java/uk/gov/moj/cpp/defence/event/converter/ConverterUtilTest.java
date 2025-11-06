package uk.gov.moj.cpp.defence.event.converter;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.defence.event.processor.TestTemplates.getDefendantDetails;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.cps.defence.Offence;
import uk.gov.moj.cpp.defence.Organisation;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConverterUtilTest {

    private static final boolean INDIVIDUAL_DEFENDANT = TRUE;
    @InjectMocks
    ConverterUtil converterUtil;

    @Test
    public void shouldTransformOffences() {

        List<uk.gov.justice.core.courts.Offence> offences = getDefendantDetails(INDIVIDUAL_DEFENDANT).get(0).getOffences();
        List<Offence> transformedOffences = converterUtil.transformOffences(offences);
        transformedOffences.forEach(offence -> {
            assertThat(offence.getArrestDate(), is(offences.get(0).getArrestDate()));
            assertThat(offence.getStartDate(), is(offences.get(0).getStartDate()));
            assertThat(offence.getId(), is(offences.get(0).getId()));
            assertThat(offence.getChargeDate(), is(offences.get(0).getChargeDate()));
            assertThat(offence.getStartDate(), is(offences.get(0).getStartDate()));
            assertThat(offence.getEndDate(), is(offences.get(0).getEndDate()));
            assertThat(offence.getCjsCode(), is(offences.get(0).getOffenceCode()));
            assertThat(offence.getOffenceCodeDetails().getCjsoffencecode(), is(offences.get(0).getOffenceCode()));
            assertThat(offence.getOffenceCodeDetails().getId(), is(offences.get(0).getOffenceDefinitionId().toString()));
            assertThat(offence.getOffenceCodeDetails().getLegislation(), is(offences.get(0).getOffenceLegislation()));
            assertThat(offence.getOffenceCodeDetails().getWelshOffenceTitle(), is(offences.get(0).getOffenceTitleWelsh()));
            assertThat(offence.getOffenceCodeDetails().getTitle(), is(offences.get(0).getOffenceTitle()));
            assertThat(offence.getOffenceCodeDetails().getModeoftrial(), is(offences.get(0).getModeOfTrial()));
            assertThat(offence.getOffenceCodeDetails().getStandardoffencewording(), is(offences.get(0).getWording()));
            assertThat(offence.getOffenceCodeDetails().getWelshstandardoffencewording(), is(offences.get(0).getWordingWelsh()));

        });
    }

    @Test
    public void shouldTransformOrganisation() {

        Defendant defendant = getDefendantDetails(!INDIVIDUAL_DEFENDANT).get(0);
        Organisation organisation = converterUtil.transformOrganisation(defendant.getLegalEntityDefendant(), defendant.getAliases());
        assertThat(organisation.getOrganisationName(), is(defendant.getLegalEntityDefendant().getOrganisation().getName()));
        assertThat(organisation.getAddress().getAddress1(), is(defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress1()));
        assertThat(organisation.getAddress().getAddress2(), is(defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress2()));
        assertThat(organisation.getAddress().getAddress3(), is(defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress3()));
        assertThat(organisation.getAddress().getAddress4(), is(defendant.getLegalEntityDefendant().getOrganisation().getAddress().getAddress4()));
        assertThat(organisation.getAddress().getAddressPostcode(), is(defendant.getLegalEntityDefendant().getOrganisation().getAddress().getPostcode()));
        assertThat(organisation.getCompanyTelephoneNumber(), is(defendant.getLegalEntityDefendant().getOrganisation().getContact().getWork()));
        assertThat(organisation.getAliasOrganisationNames().get(0), is(defendant.getAliases().get(0).getLegalEntityName()));

    }


}
