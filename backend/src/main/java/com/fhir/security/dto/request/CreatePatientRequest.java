package com.fhir.security.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePatientRequest(
        @NotBlank(message = "givenName is required")
        String givenName,
        @NotBlank(message = "familyName is required")
        String familyName,
        String birthDate,
        String gender
) {}
