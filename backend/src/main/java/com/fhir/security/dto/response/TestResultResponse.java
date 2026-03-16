package com.fhir.security.dto.response;

public record TestResultResponse(
        Long id,
        String scenarioName,
        int statusCode,
        boolean vulnerable,
        String responseBody
) {}
