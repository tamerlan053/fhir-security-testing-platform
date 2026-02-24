package com.fhir.security.controller;

import com.fhir.security.dto.PatientDto;
import com.fhir.security.mapper.PatientMapper;
import com.fhir.security.service.FhirClientService;
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
}
