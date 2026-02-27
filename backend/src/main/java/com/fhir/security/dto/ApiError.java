package com.fhir.security.dto;

import java.util.List;

public record ApiError(String error, String code, List<String> errors) {

    public static ApiError of(String error, String code) {
        return new ApiError(error, code, null);
    }

    public static ApiError validation(String error, List<String> errors) {
        return new ApiError(error, "VALIDATION_ERROR", errors);
    }
}
