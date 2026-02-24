package com.fhir.security.dto;

public record PatientDto (
    String id,
    String name,
    String birthDate,
    String gender
) {}
