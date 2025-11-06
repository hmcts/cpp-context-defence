package uk.gov.moj.cpp.defence.query.api;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.adapter.rest.exception.BadRequestException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.defence.query.view.DefenceQueryView;

import javax.json.Json;
import javax.json.JsonObject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefenceQueryApiValidationTest {

    private static final String DOB = "dateOfBirth";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String URN = "urn";
    private static final String HEARING_DATE ="hearingDate";
    private static final String IS_CIVIL = "isCivil";

    @InjectMocks
    private DefenceQueryApi defenceQueryApi;
    @Mock
    private JsonEnvelope query;
    @Mock
    private JsonEnvelope defenceClientIdEnvelope;
    @Mock
    private JsonObject responseFromView;
    @Mock
    private DefenceQueryView defenceQueryView;

    @Test
    public void shouldReturnValidResponseWhenInputParamsAreValid() {

        final JsonObject requestPayload = Json.createObjectBuilder().add(DOB, "1978-02-28")
                .add(FIRST_NAME, "John")
                .add(LAST_NAME, "Smith")
                .add(URN, "55DP0028116")
                .add(HEARING_DATE, "2022-10-15")
                .add(IS_CIVIL, true)
                .build();

        when(query.payloadAsJsonObject()).thenReturn(requestPayload);
        when(defenceClientIdEnvelope.payloadAsJsonObject()).thenReturn(responseFromView);
        when(defenceQueryView.findClientByCriteria(any())).thenReturn(defenceClientIdEnvelope);

        assertThat(defenceQueryApi.findClientByCriteria(query).toString(), is("{}"));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenDateOfBirthIsInvalid() {
        final JsonObject requestPayload = Json.createObjectBuilder().add(DOB, "1978-02-xx")
                .add(FIRST_NAME, "John")
                .add(LAST_NAME, "Smith")
                .add(URN, "55DP00281XX")
                .build();
        when(query.payloadAsJsonObject()).thenReturn(requestPayload);
        var e = assertThrows(BadRequestException.class, () -> defenceQueryApi.findClientByCriteria(query));
        assertThat(e.getMessage(), is("Invalid date format. Input date string: 1978-02-xx"));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenDateOfBirthIsOptionalForNonCivilCase() {
        final JsonObject requestPayload = Json.createObjectBuilder()
                .add(FIRST_NAME, "John")
                .add(LAST_NAME, "Smith")
                .add(URN, "55DP00281XX")
                .build();
        when(query.payloadAsJsonObject()).thenReturn(requestPayload);
        var e = assertThrows(BadRequestException.class, () -> defenceQueryApi.findClientByCriteria(query));
        assertThat(e.getMessage(), is("Invalid date format. Input date string: "));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenDateOfBirthIsWrongFormatPresentForCivilCase() {
        final JsonObject requestPayload = Json.createObjectBuilder().add(DOB, "1978-02-xx")
                .add(FIRST_NAME, "John")
                .add(LAST_NAME, "Smith")
                .add(URN, "55DP00281XX")
                .add(IS_CIVIL, true)
                .build();
        when(query.payloadAsJsonObject()).thenReturn(requestPayload);
        var e = assertThrows(BadRequestException.class, () -> defenceQueryApi.findClientByCriteria(query));
        assertThat(e.getMessage(), is("Invalid date format. Input date string: 1978-02-xx"));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenDateOfBirthIsWrongFormatPresentForCivilCase_CasesByPersonDefendant() {
        final JsonObject requestPayload = Json.createObjectBuilder().add(DOB, "1978-02-xx")
                .add(FIRST_NAME, "John")
                .add(LAST_NAME, "Smith")
                .add(URN, "55DP00281XX")
                .add(IS_CIVIL, true)
                .build();
        when(query.payloadAsJsonObject()).thenReturn(requestPayload);
        var e = assertThrows(BadRequestException.class, () -> defenceQueryApi.getCasesByPersonDefendant(query));
        assertThat(e.getMessage(), is("Invalid date format. Input date string: 1978-02-xx"));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenDateOfBirthIsOptionalForNonCivilCase_CasesByPersonDefendant() {
        final JsonObject requestPayload = Json.createObjectBuilder()
                .add(FIRST_NAME, "John")
                .add(LAST_NAME, "Smith")
                .add(URN, "55DP00281XX")
                .build();
        when(query.payloadAsJsonObject()).thenReturn(requestPayload);
        var e = assertThrows(BadRequestException.class, () -> defenceQueryApi.getCasesByPersonDefendant(query));
        assertThat(e.getMessage(), is("Invalid date format. Input date string: "));
    }
}
