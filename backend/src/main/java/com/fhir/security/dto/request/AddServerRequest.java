package com.fhir.security.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record AddServerRequest(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Base URL is required")
        @URL(message = "Base URL must be a valid URL")
        String baseUrl
) {}
