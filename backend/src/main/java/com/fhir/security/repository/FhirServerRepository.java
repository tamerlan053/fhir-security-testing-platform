package com.fhir.security.repository;

import com.fhir.security.entity.FhirServer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FhirServerRepository extends JpaRepository<FhirServer, Long> {
}
