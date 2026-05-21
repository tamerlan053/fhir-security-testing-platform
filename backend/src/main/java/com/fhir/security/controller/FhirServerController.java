package com.fhir.security.controller;

import com.fhir.security.dto.request.AddServerRequest;
import com.fhir.security.dto.response.FhirServerResponse;
import com.fhir.security.entity.FhirServer;
import com.fhir.security.mapper.FhirServerMapper;
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

    private static final Logger log = LoggerFactory.getLogger(FhirServerController.class);

    private final FhirServerService fhirServerService;

    public FhirServerController(FhirServerService fhirServerService) {
        this.fhirServerService = fhirServerService;
    }

    @PostMapping
    public ResponseEntity<FhirServerResponse> addServer(@Valid @RequestBody AddServerRequest request) {
        log.info("POST /api/servers - adding server: {}", request.name());
        FhirServer server = new FhirServer(request.name(), request.baseUrl());
        FhirServer saved = fhirServerService.addServer(server);
        return ResponseEntity.status(HttpStatus.CREATED).body(FhirServerMapper.toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeServer(@PathVariable Long id) {
        fhirServerService.removeServer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FhirServerResponse>> getAllServers() {
        List<FhirServer> servers = fhirServerService.getAllServers();
        return ResponseEntity.ok(servers.stream().map(FhirServerMapper::toResponse).toList());
    }
}
