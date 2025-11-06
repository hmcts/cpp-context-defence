package uk.gov.moj.cpp.defence.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.ArrayUtils.add;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.cps.defence.DefendantDetails.defendantDetails;
import static uk.gov.justice.cps.defence.DefendantOffender.defendantOffender;
import static uk.gov.justice.cps.defence.Offence.offence;
import static uk.gov.justice.cps.defence.OffenceCode.offenceCode;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMatcher.isHandler;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.moj.cpp.defence.command.handler.commands.ProsecutionCaseReceiveDetails.prosecutionCaseReceiveDetails;

import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.cps.defence.AddDefendant;
import uk.gov.justice.cps.defence.Cpr;
import uk.gov.justice.cps.defence.DefenceClientMappedToACase;
import uk.gov.justice.cps.defence.DefendantAdded;
import uk.gov.justice.cps.defence.DefendantDetails;
import uk.gov.justice.cps.defence.Offence;
import uk.gov.justice.cps.defence.event.ReceiveDefendantUpdateFailed;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.defence.CaseDetails;
import uk.gov.moj.cpp.defence.Defendant;
import uk.gov.moj.cpp.defence.Organisation;
import uk.gov.moj.cpp.defence.Prosecutor;
import uk.gov.moj.cpp.defence.aggregate.CaseDefenceClientMap;
import uk.gov.moj.cpp.defence.aggregate.DefenceClient;
import uk.gov.moj.cpp.defence.command.handler.commands.ProsecutionCaseReceiveDetails;
import uk.gov.moj.cpp.defence.commands.CaseDefendantChanged;
import uk.gov.moj.cpp.defence.event.listener.events.DefendantUpdateReceived;
import uk.gov.moj.cpp.defence.events.DefenceClientReceived;
import uk.gov.moj.cpp.defence.events.ProsecutionCaseReceived;
import uk.gov.moj.cpp.defence.events.SuspectAlreadyAdded;
import uk.gov.moj.defence.domain.common.UuidMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CaseDefenceClientMapCommandHandlerTest {

    private static final UUID CASE_ID = randomUUID();
    private static final UUID DEFENDANT_ID = randomUUID();
    private static final DefendantDetails DEFENDANT_DETAILS = defendantDetails().withCaseId(CASE_ID)
            .withId(DEFENDANT_ID)
            .withLastName("Smith").build();
    private static final DefendantDetails CORP_DEFENDANT_DETAILS = defendantDetails().withCaseId(CASE_ID)
            .withId(DEFENDANT_ID)
            .withOrganisation(Organisation.organisation().withOrganisationName("Aspire Limited").build()).build();
    private static final UUID COMMAND_ID = randomUUID();
    private static final String URN = "55DP0028116";
    private static final String POLICE_DEFENDANT_ID = "BS2CM01ADEF1";
    private static final String PROSECUTING_AUTHORITY = "TFL";
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            DefenceClientMappedToACase.class,
            ProsecutionCaseReceived.class,
            DefendantAdded.class,
            SuspectAlreadyAdded.class,
            ReceiveDefendantUpdateFailed.class,
            DefendantUpdateReceived.class);
    @InjectMocks
    CaseDefenceClientMapCommandHandler caseDefenceClientMapCommandHandler;
    @Mock
    private EventSource eventSource;
    @Mock
    private EventStream eventStream;
    @Mock
    private AggregateService aggregateService;

    @Test
    public void shouldHandleReceiveProsecutionCaseDetailsCommand() {
        assertThat(new CaseDefenceClientMapCommandHandler(), isHandler(COMMAND_HANDLER)
                .with(method("receiveProsecutionCaseDetails")
                        .thatHandles("defence.command.prosecution-case-receive-details")
                ));
    }

    @Test
    public void shouldHandleAddDefendantCommand() {
        assertThat(new CaseDefenceClientMapCommandHandler(), isHandler(COMMAND_HANDLER)
                .with(method("addDefendant")
                        .thatHandles("defence.command.add-defendant")
                ));
    }

    @Test
    public void shouldReceiveProsecutionCaseDetails() throws EventStreamException {

        final List<Defendant> defendants = new ArrayList<>();
        final List<Offence> offenceList = getTestOffenceList();


        final Defendant defendant = Defendant
                .defendant()
                .withId(DEFENDANT_ID.toString())
                .withLastName(DEFENDANT_DETAILS.getLastName())
                .withAsn(POLICE_DEFENDANT_ID)
                .withOffences(offenceList)
                .build();

        defendants.add(defendant);

        final ProsecutionCaseReceiveDetails prosecutionCaseReceiveDetails = prosecutionCaseReceiveDetails()
                .withCaseDetails(CaseDetails.caseDetails()
                        .withCaseId(CASE_ID)
                        .withProsecutorCaseReference(URN)
                        .withProsecutor(Prosecutor.prosecutor()
                                .withProsecutingAuthority(PROSECUTING_AUTHORITY)
                                .build())
                        .build())
                .withDefendants(defendants)
                .build();

        final Metadata metadata = metadataBuilder()
                .withName("defence.command.prosecution-case-receive-details")
                .withId(COMMAND_ID)
                .build();

        final Envelope<ProsecutionCaseReceiveDetails> envelope = envelopeFrom(metadata, prosecutionCaseReceiveDetails);

        final var aggregate = new CaseDefenceClientMap();
        when(eventSource.getStreamById(CASE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, CaseDefenceClientMap.class)).thenReturn(aggregate);

        caseDefenceClientMapCommandHandler.receiveProsecutionCaseDetails(envelope);

        final ArgumentCaptor<Stream> argumentCaptor = forClass(Stream.class);
        verify(eventStream, times(2)).append(argumentCaptor.capture());

        final Stream<JsonEnvelope> envelopeStream = (Stream<JsonEnvelope>) argumentCaptor.getValue();

        assertThat(envelopeStream, streamContaining(
                        jsonEnvelope(
                                metadata()
                                        .withName("defence.event.defence-client-mapped-to-a-case"),
                                payload().isJson(allOf(
                                                defenceClientMappedWithNoClientDetailsMatcherWithOptionalAdditionalMatcher(null)
                                        )
                                )),
                        jsonEnvelope(
                                metadata()
                                        .withName("defence.event.defendant-added"),
                                payload().isJson(allOf(
                                        suspectChargedMatcherWithOptionalAdditionalMatcher(null, offenceList))
                                ))

                )
        );

    }

    @Test
    public void shouldProcessAddDefendantCommandWhenNoPreviousMapDefenceClientToCaseCommand() throws Exception {

        final List<Offence> offenceList = getTestOffenceList();

        final AddDefendant addDefendant = AddDefendant.addDefendant()
                .withDefendantDetails(DEFENDANT_DETAILS)
                .withOffences(offenceList)
                .withPoliceDefendantId(POLICE_DEFENDANT_ID)
                .withDefendantId(DEFENDANT_ID)
                .build();

        final Metadata metadata = metadataBuilder()
                .withName("defence.command.add-defendant")
                .withId(COMMAND_ID)
                .build();

        final Envelope<AddDefendant> envelope = envelopeFrom(metadata, addDefendant);
        final var aggregate = new CaseDefenceClientMap();
        when(eventSource.getStreamById(CASE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, CaseDefenceClientMap.class)).thenReturn(aggregate);

        caseDefenceClientMapCommandHandler.addDefendant(envelope);

        final Stream<JsonEnvelope> envelopeStream = verifyAppendAndGetArgumentFrom(eventStream);

        assertThat(envelopeStream, streamContaining(
                        jsonEnvelope(
                                metadata()
                                        .withName("defence.event.defence-client-mapped-to-a-case"),
                                payload().isJson(allOf(
                                        defenceClientMappedWithNoClientDetailsMatcherWithOptionalAdditionalMatcher(withJsonPath("$.defendantDetails.lastName", equalTo("Smith"))))
                                )),
                        jsonEnvelope(
                                metadata()
                                        .withName("defence.event.defendant-added"),
                                payload().isJson(allOf(
                                        suspectChargedMatcherWithOptionalAdditionalMatcher(null, offenceList))
                                ))

                )
        );
    }

    @Test
    public void shouldProcessAddCorporateDefendantCommandWhenNoPreviousMapDefenceClientToCaseCommand() throws Exception {

        final List<Offence> offenceList = getTestOffenceList();

        final AddDefendant addDefendant = AddDefendant.addDefendant()
                .withDefendantDetails(CORP_DEFENDANT_DETAILS)
                .withOffences(offenceList)
                .withPoliceDefendantId(POLICE_DEFENDANT_ID)
                .withDefendantId(DEFENDANT_ID)
                .build();

        final Metadata metadata = metadataBuilder()
                .withName("defence.command.add-defendant")
                .withId(COMMAND_ID)
                .build();

        final Envelope<AddDefendant> envelope = envelopeFrom(metadata, addDefendant);
        final var aggregate = new CaseDefenceClientMap();
        when(eventSource.getStreamById(CASE_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, CaseDefenceClientMap.class)).thenReturn(aggregate);

        caseDefenceClientMapCommandHandler.addDefendant(envelope);

        final Stream<JsonEnvelope> envelopeStream = verifyAppendAndGetArgumentFrom(eventStream);

        assertThat(envelopeStream, streamContaining(
                        jsonEnvelope(
                                metadata()
                                        .withName("defence.event.defence-client-mapped-to-a-case"),
                                payload().isJson(allOf(
                                        defenceClientMappedWithNoClientDetailsMatcherWithOptionalAdditionalMatcher(
                                                withJsonPath("$.defendantDetails.organisation.organisationName", is("Aspire Limited"))))
                                )),
                        jsonEnvelope(
                                metadata()
                                        .withName("defence.event.defendant-added"),
                                payload().isJson(allOf(
                                        suspectChargedMatcherWithOptionalAdditionalMatcher(null, offenceList))
                                ))

                )
        );
    }

    @Test
    public void shouldReceiveDefendantUpdateFailed() throws EventStreamException {

        final uk.gov.moj.cpp.defence.commands.Defendant defendant = uk.gov.moj.cpp.defence.commands.Defendant
                .defendant()
                .withId(DEFENDANT_ID)
                .withProsecutionCaseId(randomUUID())
                .withPersonDefendant(PersonDefendant.personDefendant()
                        .withPersonDetails(Person.person().withLastName("lastName").build()).build())
                .build();


        final CaseDefendantChanged caseDefendantChanged =  CaseDefendantChanged.caseDefendantChanged()
                .withDefendant(defendant)
                .build();

        final Metadata metadata = metadataBuilder()
                .withName("defence.command.prosecution-case-receive-details")
                .withId(COMMAND_ID)
                .build();

        final Envelope<CaseDefendantChanged> envelope = envelopeFrom(metadata, caseDefendantChanged);
        when(eventSource.getStreamById(DEFENDANT_ID)).thenReturn(eventStream);
        final var aggregate = new DefenceClient();
        when(aggregateService.get(eventStream, DefenceClient.class)).thenReturn(aggregate);


        caseDefenceClientMapCommandHandler.receiveCaseDefendantChanged(envelope);

        final ArgumentCaptor<Stream> argumentCaptor = forClass(Stream.class);
        verify(eventStream, times(1)).append(argumentCaptor.capture());

        final Stream<JsonEnvelope> envelopeStream = (Stream<JsonEnvelope>) argumentCaptor.getValue();

        assertThat(envelopeStream, streamContaining(
                        jsonEnvelope(
                                metadata()
                                        .withName("defence.event.receive-defendant-update-failed"),
                                payload().isJson(allOf(
                                                notNullValue()
                                        )
                                ))

                )
        );

    }

    @Test
    public void shouldReceiveDefendantUpdateReceived() throws EventStreamException {

        final uk.gov.moj.cpp.defence.commands.Defendant defendant = uk.gov.moj.cpp.defence.commands.Defendant
                .defendant()
                .withId(DEFENDANT_ID)
                .withProsecutionCaseId(randomUUID())
                .withPersonDefendant(PersonDefendant.personDefendant()
                        .withPersonDetails(Person.person().withLastName("lastName").build()).build())
                .build();


        final CaseDefendantChanged caseDefendantChanged =  CaseDefendantChanged.caseDefendantChanged()
                .withDefendant(defendant)
                .build();

        final Metadata metadata = metadataBuilder()
                .withName("defence.command.prosecution-case-receive-details")
                .withId(COMMAND_ID)
                .build();

        final Envelope<CaseDefendantChanged> envelope = envelopeFrom(metadata, caseDefendantChanged);
        final var aggregate = new DefenceClient();
        when(eventSource.getStreamById(DEFENDANT_ID)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, DefenceClient.class)).thenReturn(aggregate);

        aggregate.apply(DefenceClientReceived.defenceClientReceived()
                .withDefendantDetails(DefendantDetails.defendantDetails()
                        .withCaseId(CASE_ID)
                        .withLastName("LAST_NAME")
                        .build()).build());

        caseDefenceClientMapCommandHandler.receiveCaseDefendantChanged(envelope);

        final ArgumentCaptor<Stream> argumentCaptor = forClass(Stream.class);
        verify(eventStream, times(1)).append(argumentCaptor.capture());

        final Stream<JsonEnvelope> envelopeStream = (Stream<JsonEnvelope>) argumentCaptor.getValue();

        assertThat(envelopeStream, streamContaining(
                        jsonEnvelope(
                                metadata()
                                        .withName("defence.event.defendant-update-received"),
                                payload().isJson(allOf(
                                                notNullValue()
                                        )
                                ))

                )
        );

    }


    private Matcher<? super ReadContext>[] defenceClientMappedWithNoClientDetailsMatcherWithOptionalAdditionalMatcher(final Matcher<? super ReadContext> additionalMatcher) {
        final Matcher[] matchers = {
                withJsonPath("$.defenceClientId", UuidMatcher.isValidUuid()),
                withJsonPath("$.defendantDetails.caseId", equalTo(CASE_ID.toString())),
                withJsonPath("$.defendantDetails.id", equalTo(DEFENDANT_ID.toString()))
        };

        if (additionalMatcher != null) {
            add(matchers, additionalMatcher);
        }
        return matchers;
    }


    private Matcher<? super ReadContext>[] suspectChargedMatcherWithOptionalAdditionalMatcher(final Matcher<? super ReadContext> additionalMatcher,
                                                                                              final List<Offence> expectedOffenceList) {

        final Offence offenceZero = expectedOffenceList.get(0);
        final Offence offenceOne = expectedOffenceList.get(1);

        final Matcher[] matchers = {
                withJsonPath("$.defendantId", equalTo(DEFENDANT_ID.toString())),
                withJsonPath("$.defendantDetails.caseId", equalTo(CASE_ID.toString())),
                withJsonPath("$.defendantDetails.id", equalTo(DEFENDANT_ID.toString())),
                withJsonPath("$.policeDefendantId", equalTo(POLICE_DEFENDANT_ID)),
                withJsonPath("$.offences.[0].id", equalTo(offenceZero.getId().toString())),
                withJsonPath("$.offences.[0].policeOffenceId", equalTo(offenceZero.getPoliceOffenceId())),
                withJsonPath("$.offences.[0].cpr.defendantOffender.year", equalTo(offenceZero.getCpr().getDefendantOffender().getYear())),
                withJsonPath("$.offences.[0].cpr.defendantOffender.organisationUnit", equalTo(offenceZero.getCpr().getDefendantOffender().getOrganisationUnit())),
                withJsonPath("$.offences.[0].cpr.defendantOffender.number", equalTo(offenceZero.getCpr().getDefendantOffender().getNumber())),
                withJsonPath("$.offences.[0].cpr.defendantOffender.checkDigit", equalTo(offenceZero.getCpr().getDefendantOffender().getCheckDigit())),
                withJsonPath("$.offences.[0].asnSequenceNumber", equalTo(offenceZero.getAsnSequenceNumber())),

                withJsonPath("$.offences.[0].offenceCodeDetails.id", equalTo(offenceZero.getOffenceCodeDetails().getId())),
                withJsonPath("$.offences.[0].offenceCodeDetails.cjsoffencecode", equalTo(offenceZero.getOffenceCodeDetails().getCjsoffencecode())),
                withJsonPath("$.offences.[0].offenceCodeDetails.title", equalTo(offenceZero.getOffenceCodeDetails().getTitle())),
                withJsonPath("$.offences.[0].offenceCodeDetails.pnldref", equalTo(offenceZero.getOffenceCodeDetails().getPnldref())),
                withJsonPath("$.offences.[0].offenceCodeDetails.offencestartdate", equalTo(offenceZero.getOffenceCodeDetails().getOffencestartdate())),
                withJsonPath("$.offences.[0].offenceCodeDetails.standardoffencewording", equalTo(offenceZero.getOffenceCodeDetails().getStandardoffencewording())),
                withJsonPath("$.offences.[0].offenceCodeDetails.standardstatementoffacts", equalTo(offenceZero.getOffenceCodeDetails().getStandardstatementoffacts())),
                withJsonPath("$.offences.[0].offenceCodeDetails.policeandcpschargingresponsibilities", equalTo(offenceZero.getOffenceCodeDetails().getPoliceandcpschargingresponsibilities())),
                withJsonPath("$.offences.[0].offenceCodeDetails.timelimitforprosecutions", equalTo(offenceZero.getOffenceCodeDetails().getTimelimitforprosecutions())),
                withJsonPath("$.offences.[0].offenceCodeDetails.misCode", equalTo(offenceZero.getOffenceCodeDetails().getMisCode())),
                withJsonPath("$.offences.[0].offenceCodeDetails.maxfinetypemagct", equalTo(offenceZero.getOffenceCodeDetails().getMaxfinetypemagct())),
                withJsonPath("$.offences.[0].offenceCodeDetails.maxfinetypecrownct", equalTo(offenceZero.getOffenceCodeDetails().getMaxfinetypecrownct())),
                withJsonPath("$.offences.[0].offenceCodeDetails.legislation", equalTo(offenceZero.getOffenceCodeDetails().getLegislation())),
                withJsonPath("$.offences.[0].offenceCodeDetails.welshOffenceTitle", equalTo(offenceZero.getOffenceCodeDetails().getWelshOffenceTitle())),
                withJsonPath("$.offences.[0].offenceCodeDetails.welshLegislation", equalTo(offenceZero.getOffenceCodeDetails().getWelshLegislation())),
                withJsonPath("$.offences.[0].offenceCodeDetails.libraCategoryCode", equalTo(offenceZero.getOffenceCodeDetails().getLibraCategoryCode())),
                withJsonPath("$.offences.[0].offenceCodeDetails.custodialIndicatorCode", equalTo(offenceZero.getOffenceCodeDetails().getCustodialIndicatorCode())),
                withJsonPath("$.offences.[0].offenceCodeDetails.dateCreated", equalTo(offenceZero.getOffenceCodeDetails().getDateCreated())),
                withJsonPath("$.offences.[0].offenceCodeDetails.dateOfLastUpdate", equalTo(offenceZero.getOffenceCodeDetails().getDateOfLastUpdate())),
                withJsonPath("$.offences.[0].offenceCodeDetails.modeoftrial", equalTo(offenceZero.getOffenceCodeDetails().getModeoftrial())),
                withJsonPath("$.offences.[0].offenceCodeDetails.modeoftrialdescription", equalTo(offenceZero.getOffenceCodeDetails().getModeoftrialdescription())),


                withJsonPath("$.offences.[0].reason", equalTo(offenceZero.getReason())),
                withJsonPath("$.offences.[0].description", equalTo(offenceZero.getDescription())),
                withJsonPath("$.offences.[0].wording", equalTo(offenceZero.getWording())),
                withJsonPath("$.offences.[0].category", equalTo(offenceZero.getCategory())),
                withJsonPath("$.offences.[0].arrestDate", equalTo(offenceZero.getArrestDate())),
                withJsonPath("$.offences.[0].startDate", equalTo(offenceZero.getStartDate())),
                withJsonPath("$.offences.[0].endDate", equalTo(offenceZero.getEndDate())),
                withJsonPath("$.offences.[0].chargeDate", equalTo(offenceZero.getChargeDate())),
                withJsonPath("$.offences.[1].id", equalTo(offenceOne.getId().toString())),
                withJsonPath("$.offences.[1].policeOffenceId", equalTo(offenceOne.getPoliceOffenceId())),
                withJsonPath("$.offences.[1].cpr.defendantOffender.year", equalTo(offenceOne.getCpr().getDefendantOffender().getYear())),
                withJsonPath("$.offences.[1].cpr.defendantOffender.organisationUnit", equalTo(offenceOne.getCpr().getDefendantOffender().getOrganisationUnit())),
                withJsonPath("$.offences.[1].cpr.defendantOffender.number", equalTo(offenceOne.getCpr().getDefendantOffender().getNumber())),
                withJsonPath("$.offences.[1].cpr.defendantOffender.checkDigit", equalTo(offenceOne.getCpr().getDefendantOffender().getCheckDigit())),
                withJsonPath("$.offences.[1].asnSequenceNumber", equalTo(offenceOne.getAsnSequenceNumber())),

                withJsonPath("$.offences.[1].offenceCodeDetails.id", equalTo(offenceOne.getOffenceCodeDetails().getId())),
                withJsonPath("$.offences.[1].offenceCodeDetails.cjsoffencecode", equalTo(offenceOne.getOffenceCodeDetails().getCjsoffencecode())),
                withJsonPath("$.offences.[1].offenceCodeDetails.title", equalTo(offenceOne.getOffenceCodeDetails().getTitle())),
                withJsonPath("$.offences.[1].offenceCodeDetails.pnldref", equalTo(offenceOne.getOffenceCodeDetails().getPnldref())),
                withJsonPath("$.offences.[1].offenceCodeDetails.offencestartdate", equalTo(offenceOne.getOffenceCodeDetails().getOffencestartdate())),
                withJsonPath("$.offences.[1].offenceCodeDetails.standardoffencewording", equalTo(offenceOne.getOffenceCodeDetails().getStandardoffencewording())),
                withJsonPath("$.offences.[1].offenceCodeDetails.standardstatementoffacts", equalTo(offenceOne.getOffenceCodeDetails().getStandardstatementoffacts())),
                withJsonPath("$.offences.[1].offenceCodeDetails.policeandcpschargingresponsibilities", equalTo(offenceOne.getOffenceCodeDetails().getPoliceandcpschargingresponsibilities())),
                withJsonPath("$.offences.[1].offenceCodeDetails.timelimitforprosecutions", equalTo(offenceOne.getOffenceCodeDetails().getTimelimitforprosecutions())),
                withJsonPath("$.offences.[1].offenceCodeDetails.misCode", equalTo(offenceOne.getOffenceCodeDetails().getMisCode())),
                withJsonPath("$.offences.[1].offenceCodeDetails.maxfinetypemagct", equalTo(offenceOne.getOffenceCodeDetails().getMaxfinetypemagct())),
                withJsonPath("$.offences.[1].offenceCodeDetails.maxfinetypecrownct", equalTo(offenceOne.getOffenceCodeDetails().getMaxfinetypecrownct())),
                withJsonPath("$.offences.[1].offenceCodeDetails.legislation", equalTo(offenceOne.getOffenceCodeDetails().getLegislation())),
                withJsonPath("$.offences.[1].offenceCodeDetails.welshOffenceTitle", equalTo(offenceOne.getOffenceCodeDetails().getWelshOffenceTitle())),
                withJsonPath("$.offences.[1].offenceCodeDetails.welshLegislation", equalTo(offenceOne.getOffenceCodeDetails().getWelshLegislation())),
                withJsonPath("$.offences.[1].offenceCodeDetails.libraCategoryCode", equalTo(offenceOne.getOffenceCodeDetails().getLibraCategoryCode())),
                withJsonPath("$.offences.[1].offenceCodeDetails.custodialIndicatorCode", equalTo(offenceOne.getOffenceCodeDetails().getCustodialIndicatorCode())),
                withJsonPath("$.offences.[1].offenceCodeDetails.dateCreated", equalTo(offenceOne.getOffenceCodeDetails().getDateCreated())),
                withJsonPath("$.offences.[1].offenceCodeDetails.dateOfLastUpdate", equalTo(offenceOne.getOffenceCodeDetails().getDateOfLastUpdate())),
                withJsonPath("$.offences.[1].offenceCodeDetails.modeoftrial", equalTo(offenceOne.getOffenceCodeDetails().getModeoftrial())),
                withJsonPath("$.offences.[1].offenceCodeDetails.modeoftrialdescription", equalTo(offenceOne.getOffenceCodeDetails().getModeoftrialdescription())),

                withJsonPath("$.offences.[1].reason", equalTo(offenceOne.getReason())),
                withJsonPath("$.offences.[1].description", equalTo(offenceOne.getDescription())),
                withJsonPath("$.offences.[1].wording", equalTo(offenceOne.getWording())),
                withJsonPath("$.offences.[1].category", equalTo(offenceOne.getCategory())),
                withJsonPath("$.offences.[1].arrestDate", equalTo(offenceOne.getArrestDate())),
                withJsonPath("$.offences.[1].startDate", equalTo(offenceOne.getStartDate())),
                withJsonPath("$.offences.[1].endDate", equalTo(offenceOne.getEndDate())),
                withJsonPath("$.offences.[1].chargeDate", equalTo(offenceOne.getChargeDate()))
        };

        if (additionalMatcher != null) {
            add(matchers, additionalMatcher);
        }

        return matchers;
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