package com.fhir.security.service;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.repository.FhirServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FhirServerService {

    private static final Logger log = LoggerFactory.getLogger(FhirServerService.class);

    private final FhirServerRepository fhirServerRepository;

    public FhirServerService(FhirServerRepository fhirServerRepository) {
        this.fhirServerRepository = fhirServerRepository;
    }

    @Transactional
    public FhirServer addServer(FhirServer server) {
        log.info("Adding FHIR server: {} - {}", server.getName(), server.getBaseUrl());
        return fhirServerRepository.save(server);
    }

    @Transactional
    public void removeServer(Long id) {
        if (!fhirServerRepository.existsById(id)) {
            throw new IllegalArgumentException("Server not found: " + id);
        }
        log.info("Removing FHIR server with id: {}", id);
        fhirServerRepository.deleteById(id);
    }

    public List<FhirServer> getAllServers() {
        return fhirServerRepository.findAll();
    }

    public FhirServer getServerById(Long id) {
        return fhirServerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + id));
    }
}
