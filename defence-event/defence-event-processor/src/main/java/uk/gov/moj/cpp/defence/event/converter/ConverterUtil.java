package uk.gov.moj.cpp.defence.event.converter;

import uk.gov.justice.core.courts.DefendantAlias;
import uk.gov.justice.cps.defence.Offence;
import uk.gov.justice.cps.defence.OffenceCode;
import uk.gov.moj.cpp.defence.Address;
import uk.gov.moj.cpp.defence.Organisation;

import java.util.ArrayList;
import java.util.List;

public class ConverterUtil {

    public Organisation transformOrganisation(uk.gov.justice.core.courts.LegalEntityDefendant legalEntityDefendant, List<DefendantAlias> aliases) {
        final List<String> organisationAliases = new ArrayList<>();
        if (aliases != null) {
            aliases.forEach(defendantAlias -> organisationAliases.add(defendantAlias.getLegalEntityName()));
        }
        if (legalEntityDefendant != null && legalEntityDefendant.getOrganisation() != null) {
            final Organisation.Builder organisationBuilder = Organisation.organisation();
            if (legalEntityDefendant.getOrganisation().getAddress() != null) {
                organisationBuilder.
                        withAddress(Address.address()
                                .withAddress1(legalEntityDefendant.getOrganisation().getAddress().getAddress1())
                                .withAddress2(legalEntityDefendant.getOrganisation().getAddress().getAddress2())
                                .withAddress3(legalEntityDefendant.getOrganisation().getAddress().getAddress3())
                                .withAddress4(legalEntityDefendant.getOrganisation().getAddress().getAddress4())
                                .withAddressPostcode(legalEntityDefendant.getOrganisation().getAddress().getPostcode())
                                .build());
            }
            if (!organisationAliases.isEmpty()) {
                organisationBuilder.withAliasOrganisationNames(organisationAliases);
            }
            if (legalEntityDefendant.getOrganisation().getContact() != null) {
                organisationBuilder.
                        withCompanyTelephoneNumber(legalEntityDefendant.getOrganisation().getContact().getWork());
            }
            organisationBuilder.withOrganisationName(legalEntityDefendant.getOrganisation().getName());
            return organisationBuilder.build();
        }
        return null;

    }


    public List<Offence> transformOffences(final List<uk.gov.justice.core.courts.Offence> offences) {
        final List<Offence> transformedOffences = new ArrayList<>();
        offences.forEach(offence ->
                transformedOffences.add(Offence.offence()
                        .withId(offence.getId())
                        .withArrestDate(offence.getArrestDate())
                        .withStartDate(offence.getStartDate())
                        .withChargeDate(offence.getChargeDate())
                        .withCjsCode(offence.getOffenceCode())
                        .withEndDate(offence.getEndDate())
                        .withOffenceCodeDetails(OffenceCode.offenceCode()
                                .withId(offence.getOffenceDefinitionId().toString())
                                .withCjsoffencecode(offence.getOffenceCode())
                                .withLegislation(offence.getOffenceLegislation())
                                .withWelshOffenceTitle(offence.getOffenceTitleWelsh())
                                .withTitle(offence.getOffenceTitle())
                                .withModeoftrial(offence.getModeOfTrial())
                                .withStandardoffencewording(offence.getWording())
                                .withWelshstandardoffencewording(offence.getWordingWelsh())
                                .build())
                        .withWording(offence.getWording())
                        .build())
        );
        return transformedOffences;
    }
}
