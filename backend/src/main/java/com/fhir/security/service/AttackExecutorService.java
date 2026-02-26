package com.fhir.security.service;

import com.fhir.security.attack.AttackScenario;
import com.fhir.security.dto.TestResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttackExecutorService {

    public TestResult execute(AttackScenario scenario, String baseUrl) {
        throw new UnsupportedOperationException("Attack execution not yet implemented");
    }

    public List<TestResult> executeAll(List<AttackScenario> scenarios, String baseUrl) {
        throw new UnsupportedOperationException("Batch attack execution not yet implemented");
    }
}
