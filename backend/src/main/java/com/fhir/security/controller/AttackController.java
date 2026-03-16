package com.fhir.security.controller;

import com.fhir.security.entity.TestRun;
import com.fhir.security.repository.TestRunRepository;
import com.fhir.security.service.AttackExecutorService;
import com.fhir.security.service.FhirServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attacks")
public class AttackController {

    private static final Logger log = LoggerFactory.getLogger(AttackController.class);

    private final AttackExecutorService attackExecutorService;
    private final FhirServerService fhirServerService;
    private final TestRunRepository testRunRepository;

    public AttackController(AttackExecutorService attackExecutorService,
                            FhirServerService fhirServerService,
                            TestRunRepository testRunRepository) {
        this.attackExecutorService = attackExecutorService;
        this.fhirServerService = fhirServerService;
        this.testRunRepository = testRunRepository;
    }

    @PostMapping("/run/{serverId}")
    public ResponseEntity<RunResult> runAttacks(@PathVariable Long serverId) {
        log.info("POST /api/attacks/run/{} - starting attack run", serverId);
        var server = fhirServerService.getServerById(serverId);
        TestRun run = attackExecutorService.executeAll(server);
        return ResponseEntity.ok(new RunResult(run.getId(), run.getStartedAt().toString()));
    }

    @GetMapping("/runs/{serverId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<TestRunSummary>> getRunsForServer(@PathVariable Long serverId) {
        log.info("GET /api/attacks/runs/{} - listing test runs", serverId);
        List<TestRun> runs = testRunRepository.findByServerIdOrderByStartedAtDesc(serverId);
        List<TestRunSummary> summaries = runs.stream()
                .map(r -> new TestRunSummary(r.getId(), r.getStartedAt().toString()))
                .toList();
        return ResponseEntity.ok(summaries);
    }

    public record RunResult(Long testRunId, String startedAt) {}
    public record TestRunSummary(Long testRunId, String startedAt) {}
}
