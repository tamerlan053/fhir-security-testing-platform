package com.fhir.security.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.springframework.stereotype.Service;

@Service
public class FhirClientService {

    private final FhirContext fhirContext;
    private IGenericClient client;

    public FhirClientService(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    public void connectToServer(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl cannot be null or empty");
        }
        this.client = fhirContext.newRestfulGenericClient(baseUrl);
    }

    public boolean testConnection() {
        if (client == null) {
            throw new IllegalStateException("Not connected. Call connectToServer(String) first.");
        }

        try {
            client.capabilities().ofType(CapabilityStatement.class).execute();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
