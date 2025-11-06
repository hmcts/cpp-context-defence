package uk.gov.moj.cpp.defence.aggregate;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.moj.cpp.defence.events.DefenceAssociationFailed;
import uk.gov.moj.cpp.defence.events.DefenceDisassociationFailed;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationAssociated;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationDisassociated;
import uk.gov.moj.cpp.defence.events.DefenceOrganisationLaareferenceReceived;
import uk.gov.moj.cpp.defence.events.DefendantDefenceAssociationLockedForLaa;
import uk.gov.moj.cpp.defence.events.RepresentationType;

import java.util.List;
import java.util.UUID;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefenceAssociationTest {

    private static final String ORGANISATION_NAME = "CompanyZ";
    private DefenceAssociation aggregate;

    @BeforeEach
    public void setUp() {
        aggregate = new DefenceAssociation();
    }

    @Test
    public void shouldReturnDefenceOrganisationAssociated() {
        final List<Object> eventStream = aggregate.associateOrganisation(randomUUID(),
                randomUUID(),
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                randomUUID().toString()).collect(toList());
        assertThat(eventStream.size(), is(1));
        final Object object = eventStream.get(0);
        assertThat(object.getClass(), is(CoreMatchers.equalTo(DefenceOrganisationAssociated.class)));
    }

    @Test
    public void shouldReturnDefenceOrganisationLaareferenceReceived() {
        final UUID defendantId = randomUUID();
        final UUID organisationId = randomUUID();
        final UUID caseId = randomUUID();

        aggregate.associateOrganisation(defendantId,
                organisationId,
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                randomUUID().toString());

        final List<Object> eventStream = aggregate.associateOrganisationForRepOrder(defendantId,
                organisationId,
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                randomUUID().toString(),
                caseId).collect(toList());
        assertThat(eventStream.size(), is(1));
        final Object object = eventStream.get(0);
        assertThat(object.getClass(), is(CoreMatchers.equalTo(DefenceOrganisationLaareferenceReceived.class)));
    }

    @Test
    public void shouldReturnDefenceOrganisationAssociatedForRepOrder() {
        final UUID defendantId = randomUUID();
        final UUID organisationId = randomUUID();
        final UUID caseId = randomUUID();

        final List<Object> eventStream = aggregate.associateOrganisationForRepOrder(defendantId,
                organisationId,
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                randomUUID().toString(),
                caseId).collect(toList());
        assertThat(eventStream.size(), is(1));
        final Object object = eventStream.get(0);
        assertThat(object.getClass(), is(CoreMatchers.equalTo(DefenceOrganisationAssociated.class)));
    }

    @Test
    public void shouldReturnDefenceOrganisationDisassociatedForRepOrder() {
        final UUID defendantId = randomUUID();
        final UUID organisationId = randomUUID();
        final UUID caseId = randomUUID();

        aggregate.associateOrganisation(defendantId,
                organisationId,
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                randomUUID().toString());

        final List<Object> eventStream = aggregate.associateOrganisationForRepOrder(defendantId,
                randomUUID(),
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                randomUUID().toString(),
                caseId).collect(toList());
        assertThat(eventStream.size(), is(2));
        assertThat(eventStream.get(0).getClass(), is(CoreMatchers.equalTo(DefenceOrganisationDisassociated.class)));
        assertThat(eventStream.get(1).getClass(), is(CoreMatchers.equalTo(DefenceOrganisationAssociated.class)));
    }

    @Test
    public void shouldReturnDefenceOrganisationDisassociatedAndAssociated() {
        final UUID defendantId = randomUUID();
        final UUID organisationId = randomUUID();

        aggregate.associateOrganisation(defendantId,
                organisationId,
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                randomUUID().toString());

        final List<Object> eventStream = aggregate.handleOrphanedDefendantAssociation(
                organisationId,
                ORGANISATION_NAME,
                defendantId,
                "1234567890",
                randomUUID().toString()).collect(toList());
        assertThat(eventStream.size(), is(2));
        assertThat(eventStream.get(0).getClass(), is(CoreMatchers.equalTo(DefenceOrganisationDisassociated.class)));
        assertThat(eventStream.get(1).getClass(), is(CoreMatchers.equalTo(DefenceOrganisationAssociated.class)));

    }

    @Test
    public void shouldReturnDefenceOrganisationAssociatedForOrphaned() {
        final UUID defendantId = randomUUID();
        final UUID organisationId = randomUUID();

        final List<Object> eventStream = aggregate.handleOrphanedDefendantAssociation(
                organisationId,
                ORGANISATION_NAME,
                defendantId,
                "1234567890",
                randomUUID().toString()).collect(toList());
        assertThat(eventStream.size(), is(1));
        assertThat(eventStream.get(0).getClass(), is(CoreMatchers.equalTo(DefenceOrganisationAssociated.class)));

    }

    @Test
    public void shouldReturnDefendantDefenceAssociationLockedForLaa() {
        final UUID defendantId = randomUUID();

        final List<Object> eventStream = aggregate.handleDefendantDefenceAssociationLocked(
                defendantId,
                "1234567890").collect(toList());
        assertThat(eventStream.size(), is(1));
        assertThat(eventStream.get(0).getClass(), is(CoreMatchers.equalTo(DefendantDefenceAssociationLockedForLaa.class)));

    }


    @Test
    public void shouldReturnEmptyStreamWhenOrgAlreadyAssociated() {
        final UUID defendantId = randomUUID();
        final UUID organisationId = randomUUID();
        aggregate.associateOrganisation(defendantId,
                organisationId,
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                randomUUID().toString());
        final List<Object> eventStream = aggregate.associateOrganisation(defendantId,
                organisationId,
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                randomUUID().toString()).collect(toList());

        assertThat(eventStream.size(), is(1));
        final Object object = eventStream.get(0);
        assertThat(object.getClass(), is(CoreMatchers.equalTo(DefenceAssociationFailed.class)));

    }

    @Test
    public void shouldReturnDefenceOrganisationDisassociated() {
        UUID defendantId = randomUUID();
        UUID organisationId = randomUUID();
        UUID userId = randomUUID();
        UUID caseId = randomUUID();
        aggregate.associateOrganisation(defendantId,
                organisationId,
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                userId.toString()).collect(toList());


        final List<Object> eventStream = aggregate.disassociateOrganisation(defendantId,
                organisationId, randomUUID(), userId, false).collect(toList());
        assertThat(eventStream.size(), is(1));
        final Object object = eventStream.get(0);
        assertThat(object.getClass(), is(CoreMatchers.equalTo(DefenceOrganisationDisassociated.class)));
    }

    @Test
    public void shouldReturnFailedDefenceOrganisationDisassociated() {
        UUID defendantId = randomUUID();
        UUID organisationId = randomUUID();
        UUID userId = randomUUID();
        aggregate.associateOrganisation(defendantId,
                organisationId,
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                userId.toString()).collect(toList());
        aggregate.disassociateOrganisation(defendantId,
                organisationId, randomUUID(), userId, false).collect(toList());

        final List<Object> eventStream = aggregate.disassociateOrganisation(defendantId,
                organisationId, randomUUID(), userId, false).collect(toList());
        assertThat(eventStream.size(), is(1));
        final Object object = eventStream.get(0);
        assertThat(object.getClass(), is(CoreMatchers.equalTo(DefenceDisassociationFailed.class)));
    }

    @Test
    public void shouldReturnFailedDefenceOrganisationDisassociatedWhenOrganisationidsDontMatch() {
        UUID defendantId = randomUUID();
        UUID existingAssociatedOrganisationId = randomUUID();
        UUID differentOrganisationId = randomUUID();
        UUID userId = randomUUID();
        aggregate.associateOrganisation(defendantId,
                existingAssociatedOrganisationId,
                ORGANISATION_NAME,
                RepresentationType.REPRESENTATION_ORDER.toString(),
                "1234567890",
                userId.toString()).collect(toList());

        aggregate.disassociateOrganisation(defendantId,
                differentOrganisationId, randomUUID(), userId, false).collect(toList());

        final List<Object> eventStream = aggregate.disassociateOrganisation(defendantId,
                differentOrganisationId, randomUUID(), userId, false).collect(toList());
        assertThat(eventStream.size(), is(1));
        final Object object = eventStream.get(0);
        assertThat(object.getClass(), is(CoreMatchers.equalTo(DefenceDisassociationFailed.class)));
    }
}