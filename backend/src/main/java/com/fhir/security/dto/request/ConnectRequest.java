package com.fhir.security.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ConnectRequest(
        @NotBlank(message = "Base URL is required")
        @URL(message = "Base URL must be a valid URL")
        String baseUrl
) {}
