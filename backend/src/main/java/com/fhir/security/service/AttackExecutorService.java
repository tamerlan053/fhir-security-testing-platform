package com.fhir.security.service;

import com.fhir.security.attack.AttackRegistry;
import com.fhir.security.attack.AttackResult;
import com.fhir.security.attack.AttackVectorCatalog;
import com.fhir.security.attack.ExecutableAttack;
import com.fhir.security.attack.LeakageExposure;
import com.fhir.security.attack.ResponseBodyLeakageAnalyzer;
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
            AttackResult raw = scenario.execute(server);
            AttackResult enriched = enrichForPersistence(scenario, raw);

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
            tr.setStatusCode(enriched.statusCode());
            tr.setResponseBody(enriched.responseBody());
            tr.setClassification(enriched.classification());
            tr.setReason(enriched.reason());
            tr.setSeverity(enriched.severity());
            tr.setVulnerable(enriched.vulnerable());
            tr.setAttackVectorIds(enriched.attackVectorIds());
            tr.setLeakageExposure(enriched.leakageExposure());
            testResultRepository.save(tr);
        }

        log.info("Executed {} attacks for server {}", registry.getScenarios().size(), server.getName());
        return run;
    }

    /**
     * Merge catalog vector tags with any per-probe tags from the attack, and classify response-body leakage (Week 10).
     */
    private static AttackResult enrichForPersistence(ExecutableAttack scenario, AttackResult result) {
        String catalog = AttackVectorCatalog.tagsFor(scenario.getClass());
        String mergedVectors = AttackVectorCatalog.mergeIds(result.attackVectorIds(), catalog);
        LeakageExposure fromBody = ResponseBodyLeakageAnalyzer.analyze(result.statusCode(), result.responseBody());
        LeakageExposure leak = ResponseBodyLeakageAnalyzer.worst(result.leakageExposure(), fromBody);
        return AttackResult.of(
                result.statusCode(),
                result.responseBody(),
                result.classification(),
                result.reason(),
                result.severity(),
                mergedVectors,
                leak
        );
    }
}
