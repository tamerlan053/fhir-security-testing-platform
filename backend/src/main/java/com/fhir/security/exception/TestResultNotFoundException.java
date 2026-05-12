package com.fhir.security.exception;

public class TestResultNotFoundException extends RuntimeException {

    public TestResultNotFoundException(Long testResultId) {
        super("Test result not found: " + testResultId);
    }
}
