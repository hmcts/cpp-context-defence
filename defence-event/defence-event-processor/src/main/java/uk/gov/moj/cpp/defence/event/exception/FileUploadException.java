package uk.gov.moj.cpp.defence.event.exception;

public class FileUploadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileUploadException() {
    }

    public FileUploadException(final Throwable cause) {
        super(cause);
    }
}

