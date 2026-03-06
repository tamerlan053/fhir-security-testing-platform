package com.fhir.security.dto;

public record FhirServerResponse(
        Long id,
        String name,
        String baseUrl,
        String authenticationType
) {}
