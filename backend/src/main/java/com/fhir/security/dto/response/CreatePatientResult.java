package com.fhir.security.dto.response;

import java.util.List;

public record CreatePatientResult(
        boolean success,
        String patientId,
        int statusCode,
        String message,
        List<String> validationErrors
) {}
