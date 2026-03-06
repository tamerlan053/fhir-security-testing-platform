package com.fhir.security.repository;

import com.fhir.security.entity.AttackScenario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttackScenarioRepository extends JpaRepository<AttackScenario, Long> {
}
