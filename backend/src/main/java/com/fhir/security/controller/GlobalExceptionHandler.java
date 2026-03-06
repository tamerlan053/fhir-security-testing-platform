package com.fhir.security.controller;

import com.fhir.security.dto.ApiError;
import com.fhir.security.exception.FhirServerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException e) {
        String message = e.getMessage() != null && e.getMessage().contains("Not connected")
                ? "Not connected to FHIR server"
                : e.getMessage();
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiError.of(message, "NOT_CONNECTED"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException e) {
        if (e.getMessage() != null && e.getMessage().contains("Server not found")) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiError.of(e.getMessage(), "SERVER_NOT_FOUND"));
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(e.getMessage(), "INVALID_ARGUMENT"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.validation("Invalid request body", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of("Invalid request body", "INVALID_JSON"));
    }

    @ExceptionHandler(FhirServerException.class)
    public ResponseEntity<ApiError> handleFhirServer(FhirServerException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ApiError.of(e.getMessage(), "FHIR_SERVER_ERROR"));
    }
}
