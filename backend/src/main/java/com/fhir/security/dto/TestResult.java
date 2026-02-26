package com.fhir.security.dto;

import java.util.List;

public record TestResult(
        String scenarioId,
        String scenarioName,
        boolean vulnerabilityFound,
        String severity,
        String message,
        List<String> evidence
) {}