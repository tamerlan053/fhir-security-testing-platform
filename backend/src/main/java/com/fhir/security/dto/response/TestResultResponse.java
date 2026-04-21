package com.fhir.security.dto.response;

public record TestResultResponse(
        Long id,
        String scenarioName,
        int statusCode,
        boolean vulnerable,
        String classification,
        String reason,
        String severity,
        String responseBody
) {}
