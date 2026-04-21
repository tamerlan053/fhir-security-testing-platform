package com.fhir.security.service;

import com.fhir.security.attack.AttackRegistry;
import com.fhir.security.attack.AttackResult;
import com.fhir.security.attack.ExecutableAttack;
import com.fhir.security.entity.AttackScenario;
import com.fhir.security.entity.FhirServer;
import com.fhir.security.entity.TestResult;
import com.fhir.security.entity.TestRun;
import com.fhir.security.repository.AttackScenarioRepository;
import com.fhir.security.repository.TestResultRepository;
import com.fhir.security.repository.TestRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AttackExecutorService {

    private static final Logger log = LoggerFactory.getLogger(AttackExecutorService.class);

    private final AttackRegistry registry;
    private final TestRunRepository testRunRepository;
    private final TestResultRepository testResultRepository;
    private final AttackScenarioRepository attackScenarioRepository;

    public AttackExecutorService(AttackRegistry registry,
                                TestRunRepository testRunRepository,
                                TestResultRepository testResultRepository,
                                AttackScenarioRepository attackScenarioRepository) {
        this.registry = registry;
        this.testRunRepository = testRunRepository;
        this.testResultRepository = testResultRepository;
        this.attackScenarioRepository = attackScenarioRepository;
    }

    @Transactional
    public TestRun executeAll(FhirServer server) {
        TestRun run = new TestRun(server, LocalDateTime.now());
        testRunRepository.save(run);

        for (ExecutableAttack scenario : registry.getScenarios()) {
            AttackResult result = scenario.execute(server);

            AttackScenario entityScenario = attackScenarioRepository
                    .findByName(scenario.getName())
                    .orElseGet(() -> attackScenarioRepository.save(
                            new AttackScenario(
                                    scenario.getName(),
                                    scenario.getDescription(),
                                    "MEDIUM"
                            )
                    ));

            TestResult tr = new TestResult();
            tr.setTestRun(run);
            tr.setScenario(entityScenario);
            tr.setStatusCode(result.statusCode());
            tr.setResponseBody(result.responseBody());
            tr.setClassification(result.classification());
            tr.setReason(result.reason());
            tr.setSeverity(result.severity());
            tr.setVulnerable(result.vulnerable());
            testResultRepository.save(tr);
        }

        log.info("Executed {} attacks for server {}", registry.getScenarios().size(), server.getName());
        return run;
    }
}
