package com.fhir.security.dto;

import jakarta.validation.constraints.NotBlank;

public record AddServerRequest (
    @NotBlank(message = "Name is required")
    String name,
    @NotBlank(message = "Base URL is required")
    String baseUrl,
    String authenticationType
) {}
