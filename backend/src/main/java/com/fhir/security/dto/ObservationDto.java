package com.fhir.security.dto;

public record ObservationDto (
    String id,
    String patientId,
    String code,
    String display,
    String value,
    String unit,
    String effectiveDateTime,
    String status
) {}
