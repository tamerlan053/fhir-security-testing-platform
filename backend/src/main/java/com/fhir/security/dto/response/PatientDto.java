package com.fhir.security.dto.response;

public record PatientDto(
        String id,
        String name,
        String birthDate,
        String gender
) {}
