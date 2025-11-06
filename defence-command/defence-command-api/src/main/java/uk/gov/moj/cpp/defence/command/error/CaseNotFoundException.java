package uk.gov.moj.cpp.defence.command.error;

public class CaseNotFoundException extends RuntimeException {

    public CaseNotFoundException(final String message) {
        super(message);
    }
}
