package com.fhir.security.dto.response;

public record ObservationDto(
        String id,
        String patientId,
        String code,
        String display,
        String value,
        String unit,
        String effectiveDateTime,
        String status
) {}
