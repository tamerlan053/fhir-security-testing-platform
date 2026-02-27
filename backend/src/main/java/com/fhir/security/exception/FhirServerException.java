package com.fhir.security.exception;

public class FhirServerException extends RuntimeException {

    public FhirServerException(String message) {
        super(message);
    }

    public FhirServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
