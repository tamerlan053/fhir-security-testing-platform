package com.fhir.security.controller;

import com.fhir.security.dto.ObservationDto;
import com.fhir.security.dto.PatientDto;
import com.fhir.security.mapper.ObservationMapper;
import com.fhir.security.mapper.PatientMapper;
import com.fhir.security.service.FhirClientService;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fhir")
public class FhirTestController {

    private final FhirClientService fhirClientService;

    public FhirTestController(FhirClientService fhirClientService) {
        this.fhirClientService = fhirClientService;
    }

    @PostMapping("/connect")
    public String connect(@RequestParam String baseUrl) {
        fhirClientService.connectToServer(baseUrl);
        return "Connected to " + baseUrl;
    }

    @GetMapping("/test")
    public boolean test() {
        return fhirClientService.testConnection();
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
}
