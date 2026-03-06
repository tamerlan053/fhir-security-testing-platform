package com.fhir.security.controller;

import com.fhir.security.dto.AddServerRequest;
import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.FhirServerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
public class FhirServerController {

    private static final Logger log = LoggerFactory.getLogger(FhirServerService.class);

    private final FhirServerService fhirServerService;

    public FhirServerController(FhirServerService fhirServerService) {
        this.fhirServerService = fhirServerService;
    }

    @PostMapping
    public ResponseEntity<FhirServer> addServer(@Valid @RequestBody AddServerRequest request) {
        log.info("POST /api/servers - adding server: {}", request.name());
        FhirServer server = new  FhirServer(
                request.name(),
                request.baseUrl(),
                request.authenticationType()
        );
        FhirServer saved =  fhirServerService.addServer(server);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<FhirServer> removeServer(@PathVariable Long id) {
        fhirServerService.removeServer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FhirServer>> getAllServers() {
        List<FhirServer> servers = fhirServerService.getAllServers();
        return ResponseEntity.ok(servers);
    }
}
