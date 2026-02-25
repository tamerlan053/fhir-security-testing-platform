package com.fhir.security.dto;

public record CreatePatientRequest (
    String givenName,
    String familyName,
    String birthDate,
    String gender
) {}
