package com.fhir.security.repository;

import com.fhir.security.attack.AttackScenario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttackScenarioRepository extends JpaRepository<AttackScenario, Long> {
}
