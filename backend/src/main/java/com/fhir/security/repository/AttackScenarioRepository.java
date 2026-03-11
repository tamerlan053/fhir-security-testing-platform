package com.fhir.security.repository;

import com.fhir.security.entity.AttackScenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttackScenarioRepository extends JpaRepository<AttackScenario, Long> {

    Optional<AttackScenario> findByName(String name);
}
