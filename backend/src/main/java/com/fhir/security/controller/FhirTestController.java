package com.fhir.security.controller;

import com.fhir.security.dto.CreatePatientRequest;
import com.fhir.security.dto.CreatePatientResult;
import com.fhir.security.dto.ObservationDto;
import com.fhir.security.dto.PatientDto;
import com.fhir.security.mapper.ObservationMapper;
import com.fhir.security.mapper.PatientMapper;
import com.fhir.security.service.FhirClientService;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fhir")
public class FhirTestController {

    private static final Logger log = LoggerFactory.getLogger(FhirTestController.class);

    private final FhirClientService fhirClientService;

    public FhirTestController(FhirClientService fhirClientService) {
        this.fhirClientService = fhirClientService;
    }

    @PostMapping("/connect")
    public String connect(@RequestParam String baseUrl) {
        log.info("Connecting to FHIR server: {}", baseUrl);
        fhirClientService.connectToServer(baseUrl);
        return "Connected to " + baseUrl;
    }

    @GetMapping("/test")
    public boolean test() {
        boolean connected = fhirClientService.testConnection();
        log.debug("Connection test result: {}", connected);
        return connected;
    }

    @GetMapping("/Patient")
    public List<PatientDto> getPatients(@RequestParam(defaultValue = "10") int count) {
        List<Patient> patients = fhirClientService.fetchPatients(count);
        return patients.stream()
                .map(PatientMapper::toDto)
                .toList();
    }

    @GetMapping("/Observation")
    public List<ObservationDto> getObservations(@RequestParam(defaultValue = "10") int count) {
        List<Observation> observations = fhirClientService.fetchObservations(count);
        return observations.stream()
                .map(ObservationMapper::toDto)
                .toList();
    }

    @GetMapping(value = "/Observation", params = "patient")
    public List<ObservationDto> getObservationsByPatient(
            @RequestParam String patient,
            @RequestParam(defaultValue = "50") int count) {
        List<Observation> observations = fhirClientService.fetchObservationsByPatientId(patient, count);
        return observations.stream()
                .map(ObservationMapper::toDto)
                .toList();
    }

    @PostMapping("/Patient")
    public ResponseEntity<CreatePatientResult> createPatient(@RequestBody CreatePatientRequest request) {
        log.info("Creating patient: {} {}", request.givenName(), request.familyName());
        Patient patient = PatientMapper.fromCreateRequest(request);
        CreatePatientResult result = fhirClientService.createPatient(patient);
        log.debug("Create patient result: success={}, id={}", result.success(), result.patientId());
        return ResponseEntity.status(result.statusCode()).body(result);
    }
}
