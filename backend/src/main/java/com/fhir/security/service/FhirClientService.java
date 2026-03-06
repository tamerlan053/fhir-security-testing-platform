package com.fhir.security.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import com.fhir.security.dto.response.CreatePatientResult;
import com.fhir.security.exception.FhirServerException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.*;
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
        } catch (BaseServerResponseException e) {
            log.warn("Connection test failed: statusCode={}, message={}", e.getStatusCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Connection test failed: {}", e.getMessage());
            return false;
        }
    }

    public List<Patient> fetchPatients(int count) {
        if (client == null) {
            throw new IllegalStateException("Not connected. Call connectToServer(String) first.");
        }
        try {
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
        } catch (BaseServerResponseException e) {
            log.warn("Failed to fetch patients: statusCode={}, message={}", e.getStatusCode(), e.getMessage());
            throw new FhirServerException("FHIR server error or unreachable", e);
        } catch (Exception e) {
            log.error("Failed to fetch patients: {}", e.getMessage(), e);
            throw new FhirServerException("FHIR server error or unreachable", e);
        }
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
        } catch (BaseServerResponseException e) {
            log.warn("Failed to fetch observations: statusCode={}, message={}", e.getStatusCode(), e.getMessage());
            throw new FhirServerException("FHIR server error or unreachable", e);
        } catch (Exception e) {
            log.warn("Failed to fetch observations: {}", e.getMessage());
            throw new FhirServerException("FHIR server error or unreachable", e);
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
        } catch (BaseServerResponseException e) {
            log.warn("Failed to fetch observations for patient {}: statusCode={}, message={}", patientId, e.getStatusCode(), e.getMessage());
            throw new FhirServerException("FHIR server error or unreachable", e);
        } catch (Exception e) {
            log.warn("Failed to fetch observations for patient {}: {}", patientId, e.getMessage());
            throw new FhirServerException("FHIR server error or unreachable", e);
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

    public CreatePatientResult createPatient(Patient patient) {
        if (client == null) {
            throw new IllegalStateException("Not connected. Call connectToServer(String) first.");
        }
        try {
            MethodOutcome outcome = client.create().resource(patient).execute();

            boolean created = Boolean.TRUE.equals(outcome.getCreated());
            int statusCode = created ? 201 : 200;
            String id = outcome.getId() != null ? outcome.getId().getIdPart() : null;

            log.info("Patient create response: statusCode={}, created={}, id={}", statusCode, created, id);

            return new CreatePatientResult(true, id, statusCode, null, List.of());
        } catch (BaseServerResponseException e) {
            int statusCode = e.getStatusCode();
            String message = e.getMessage();
            List<String> validationErrors = extractValidationErrors(e.getOperationOutcome());

            log.warn("Patient create failed: statusCode={}, message={}", statusCode, message);
            validationErrors.forEach(err -> log.warn("Validation error: {}", err));

            return new CreatePatientResult(false, null, statusCode, message, validationErrors);
        } catch (Exception e) {
            log.error("Patient create failed with unexpected error", e);
            return new CreatePatientResult(false, null, 500, e.getMessage(), List.of());
        }
    }

    private List<String> extractValidationErrors(IBaseOperationOutcome outcome) {
        if (outcome == null) {
            return List.of();
        }
        if (!(outcome instanceof OperationOutcome oo)) {
            return List.of();
        }
        if (!oo.hasIssue()) {
            return List.of();
        }

        return oo.getIssue().stream()
                .map(issue -> {
                    String severity = issue.getSeverity() != null ? issue.getSeverity().getDisplay() : "?";
                    String code = issue.getCode() != null ? issue.getCode().getDisplay() : "?";
                    String diag = issue.getDiagnostics();
                    String details = issue.hasDetails() && issue.getDetails().hasText() ? issue.getDetails().getText() : null;
                    return String.format("[%s] %s: %s", severity, code, diag != null ? diag : (details != null ? details : ""));
                })
                .toList();
    }
}
