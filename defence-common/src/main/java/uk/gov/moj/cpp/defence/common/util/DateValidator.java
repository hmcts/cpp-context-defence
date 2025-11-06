package uk.gov.moj.cpp.defence.common.util;

import static java.time.LocalDate.parse;

import uk.gov.justice.services.adapter.rest.exception.BadRequestException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateValidator {

    private DateValidator(){}

    public static LocalDate validateDateString(final String dateString) {
        try {
            return parse(dateString);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format. Input date string: " + dateString, e);
        }

    }
}
