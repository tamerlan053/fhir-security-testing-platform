package com.fhir.security.exception;

public class TestRunNotFoundException extends RuntimeException {

    public TestRunNotFoundException(Long testRunId) {
        super("Test run not found: " + testRunId);
    }
}
