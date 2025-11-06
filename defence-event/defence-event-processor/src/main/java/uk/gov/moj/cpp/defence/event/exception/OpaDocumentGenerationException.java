package uk.gov.moj.cpp.defence.event.exception;

public class OpaDocumentGenerationException extends RuntimeException {

    public OpaDocumentGenerationException(final String message) {
        super(message);
    }

    public OpaDocumentGenerationException(final String message, final Exception e) {
        super(message, e);
    }
}
