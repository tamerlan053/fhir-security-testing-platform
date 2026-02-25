package com.fhir.security.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.fhir.security.dto.ObservationDto;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FhirClientService {

    private final FhirContext fhirContext;
    private IGenericClient client;

    private static final Logger log = LoggerFactory.getLogger(FhirClientService.class);

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

    public List<Patient> fetchPatients(int count) {
        if (client == null) {
            throw new IllegalStateException("Not connected. Call connectToServer(String) first.");
        }
        Bundle bundle = client.search()
                .forResource(Patient.class)
                .count(count)
                .returnBundle(Bundle.class)
                .execute();
        List<Patient> patients = bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(r -> r instanceof Patient)
                .map(r -> (Patient) r)
                .collect(Collectors.toList());

        log.info("Fetched {} patients from FHIR server", patients.size());
        patients.forEach(p -> log.debug("Patient: id={}, name={}", p.getId(), p.getName().isEmpty() ? "N/A" : p.getNameFirstRep().getNameAsSingleString()));

        return patients;
    }

    public List<Observation> fetchObservations(int count) {
        if (client == null) {
            throw new IllegalStateException("Not connected. Call connectToServer(String) first.");
        }
        try {
            Bundle bundle = client.search()
                    .forResource(Observation.class)
                    .count(count)
                    .returnBundle(Bundle.class)
                    .execute();
            return extractObservations(bundle);
        } catch (Exception e) {
            log.warn("Failed to fetch obsercations {}", e.getMessage());
            return List.of();
        }
    }

    public List<Observation> fetchObservationsByPatientId(String patientId, int count) {
        if (client == null) {
            throw new IllegalStateException("Not connected. Call connectToServer(String) first.");
        }
        if (patientId == null || patientId.isBlank()) {
            log.warn("PatientId is null or blank");
            return List.of();
        }
        try {
            String patientRef = patientId.contains("/") ? patientId : "Patient/" +  patientId;
            Bundle bundle = client.search()
                    .forResource(Observation.class)
                    .where(Observation.SUBJECT.hasId(patientRef))
                    .count(count)
                    .returnBundle(Bundle.class)
                    .execute();
            return extractObservations(bundle);
        } catch (Exception e) {
            log.warn("Failed to fetch obsercations for patient {}", patientId, e.getMessage());
            return List.of();
        }
    }

    private List<Observation> extractObservations(Bundle bundle) {
        if (bundle == null || !bundle.hasEntry()) {
            return List.of();
        }
        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(r -> r instanceof Observation)
                .map(r -> (Observation) r)
                .collect(Collectors.toList());
    }
}
