package uk.gov.moj.cpp.defence.event.converter;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.defence.event.processor.TestTemplates.createProsecutionCaseCreatedEvent;

import uk.gov.moj.cpp.defence.event.processor.commands.ProsecutionCaseReceiveDetails;
import uk.gov.moj.cpp.progression.json.schema.event.ProsecutionCaseCreated;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProsecutionCaseConverterTest {
    private final boolean SJP_CASE = TRUE;
    private final boolean IS_CIVIL = TRUE;
    private final boolean IS_GROUP_MEMBER = TRUE;
    @InjectMocks
    ProsecutionCaseConverter prosecutionCaseConverter;
    @Spy
    ConverterUtil converterUtil;

    @Test
    public void shouldConvertToProsecutionCaseReceiveDetails() {
        ProsecutionCaseCreated prosecutionCaseCreated = createProsecutionCaseCreatedEvent(!SJP_CASE, !IS_CIVIL, !IS_GROUP_MEMBER);
        ProsecutionCaseReceiveDetails prosecutionCaseReceiveDetails = prosecutionCaseConverter.convertToProsecutionCaseReceiveDetails(prosecutionCaseCreated.getProsecutionCase());
        assertThat(prosecutionCaseReceiveDetails.getCaseDetails().getIsCivil(), is(!IS_CIVIL));
        assertThat(prosecutionCaseReceiveDetails.getCaseDetails().getIsGroupMember(), is(!IS_GROUP_MEMBER));
        assertThat(prosecutionCaseReceiveDetails.getCaseDetails().getCaseId(), is(prosecutionCaseCreated.getProsecutionCase().getId()));
        assertThat(prosecutionCaseReceiveDetails.getCaseDetails().getProsecutorCaseReference(), is(prosecutionCaseCreated.getProsecutionCase().getProsecutionCaseIdentifier().getProsecutionAuthorityReference()));
        assertThat(prosecutionCaseReceiveDetails.getCaseDetails().getProsecutor().getProsecutionAuthorityId(), is(prosecutionCaseCreated.getProsecutionCase().getProsecutionCaseIdentifier().getProsecutionAuthorityId()));

        assertThat(prosecutionCaseReceiveDetails.getDefendants().size(), is(prosecutionCaseCreated.getProsecutionCase().getDefendants().size()));
        assertThat(prosecutionCaseReceiveDetails.getDefendants().get(0).getFirstName(), is(prosecutionCaseCreated.getProsecutionCase().getDefendants().get(0).getPersonDefendant().getPersonDetails().getFirstName()));
        assertThat(prosecutionCaseReceiveDetails.getDefendants().get(0).getLastName(), is(prosecutionCaseCreated.getProsecutionCase().getDefendants().get(0).getPersonDefendant().getPersonDetails().getLastName()));
        assertThat(prosecutionCaseReceiveDetails.getDefendants().get(0).getDateOfBirth(), is(LocalDate.parse(prosecutionCaseCreated.getProsecutionCase().getDefendants().get(0).getPersonDefendant().getPersonDetails().getDateOfBirth())));
        assertThat(prosecutionCaseReceiveDetails.getDefendants().get(0).getAsn(), is(prosecutionCaseCreated.getProsecutionCase().getDefendants().get(0).getProsecutionAuthorityReference()));
        assertThat(prosecutionCaseReceiveDetails.getDefendants().get(0).getProsecutorDefendantReference(), is(prosecutionCaseCreated.getProsecutionCase().getDefendants().get(0).getProsecutionAuthorityReference()));

    }

    @Test
    public void shouldConvertToProsecutionCaseReceiveDetailsForSjp() {
        ProsecutionCaseCreated prosecutionCaseCreated = createProsecutionCaseCreatedEvent(SJP_CASE, IS_CIVIL, IS_GROUP_MEMBER);
        ProsecutionCaseReceiveDetails prosecutionCaseReceiveDetails = prosecutionCaseConverter.convertToProsecutionCaseReceiveDetails(prosecutionCaseCreated.getProsecutionCase());
        assertThat(prosecutionCaseReceiveDetails.getCaseDetails().getIsCivil(), is(IS_CIVIL));
        assertThat(prosecutionCaseReceiveDetails.getCaseDetails().getIsGroupMember(), is(IS_GROUP_MEMBER));
        assertThat(prosecutionCaseReceiveDetails.getCaseDetails().getProsecutorCaseReference(), is(prosecutionCaseCreated.getProsecutionCase().getProsecutionCaseIdentifier().getCaseURN()));
    }

}
