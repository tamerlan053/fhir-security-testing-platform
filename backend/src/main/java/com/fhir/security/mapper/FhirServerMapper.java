package com.fhir.security.mapper;

import com.fhir.security.dto.response.FhirServerResponse;
import com.fhir.security.entity.FhirServer;

public class FhirServerMapper {

    public static FhirServerResponse toResponse(FhirServer server) {
        return new FhirServerResponse(
                server.getId(),
                server.getName(),
                server.getBaseUrl()
        );
    }
}
