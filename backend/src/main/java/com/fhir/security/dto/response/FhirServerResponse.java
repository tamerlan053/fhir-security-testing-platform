package com.fhir.security.dto.response;

public record FhirServerResponse(
        Long id,
        String name,
        String baseUrl
) {}
