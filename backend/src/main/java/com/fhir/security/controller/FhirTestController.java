package com.fhir.security.controller;

import com.fhir.security.service.FhirClientService;
import org.springframework.web.bind.annotation.*;

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
}
