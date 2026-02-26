package com.fhir.security.service;

import com.fhir.security.attack.AttackScenario;
import com.fhir.security.dto.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttackExecutorService {

    private static final Logger log = LoggerFactory.getLogger(AttackExecutorService.class);

    public TestResult execute(AttackScenario scenario, String baseUrl) {
        log.debug("Attack execution requested for scenario against {}", baseUrl);
        throw new UnsupportedOperationException("Attack execution not yet implemented");
    }

    public List<TestResult> executeAll(List<AttackScenario> scenarios, String baseUrl) {
        log.debug("Batch attack execution requested: {} scenarios against {}", scenarios.size(), baseUrl);
        throw new UnsupportedOperationException("Batch attack execution not yet implemented");
    }
}
