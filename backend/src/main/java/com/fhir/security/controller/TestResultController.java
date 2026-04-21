package com.fhir.security.controller;

import com.fhir.security.attack.AttackClassification;
import com.fhir.security.dto.response.CompareResponse;
import com.fhir.security.dto.response.TestResultResponse;
import com.fhir.security.dto.response.TestRunResponse;
import com.fhir.security.entity.TestResult;
import com.fhir.security.entity.TestRun;
import com.fhir.security.exception.TestRunNotFoundException;
import com.fhir.security.repository.TestRunRepository;
import com.fhir.security.service.TestComparisonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/results")
public class TestResultController {

    private static final Logger log = LoggerFactory.getLogger(TestResultController.class);

    private final TestRunRepository testRunRepository;
    private final TestComparisonService testComparisonService;

    public TestResultController(TestRunRepository testRunRepository,
                                TestComparisonService testComparisonService) {
        this.testRunRepository = testRunRepository;
        this.testComparisonService = testComparisonService;
    }

    @GetMapping("/compare")
    @Transactional(readOnly = true)
    public ResponseEntity<CompareResponse> compare(@RequestParam("serverIds") String serverIdsParam) {
        log.info("GET /api/results/compare?serverIds={}", serverIdsParam);
        List<Long> serverIds = parseServerIds(serverIdsParam);
        return ResponseEntity.ok(testComparisonService.compareRunsForServers(serverIds));
    }

    private static List<Long> parseServerIds(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("serverIds query parameter is required (comma-separated ids)");
        }
        List<Long> out = new ArrayList<>();
        for (String part : raw.split(",")) {
            String t = part.trim();
            if (t.isEmpty()) {
                continue;
            }
            try {
                out.add(Long.parseLong(t));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid server id: " + t);
            }
        }
        if (out.isEmpty()) {
            throw new IllegalArgumentException("serverIds must contain at least one id");
        }
        return out;
    }

    @GetMapping("/{testRunId:\\d+}")
    @Transactional(readOnly = true)
    public ResponseEntity<TestRunResponse> getResults(@PathVariable Long testRunId) {
        log.info("GET /api/results/{} - fetching test run results", testRunId);
        TestRun run = testRunRepository.findById(testRunId)
                .orElseThrow(() -> new TestRunNotFoundException(testRunId));

        List<TestResultResponse> results = run.getTestResults().stream()
                .map(this::toResponse)
                .toList();

        TestRunResponse response = new TestRunResponse(
                run.getId(),
                run.getServer().getId(),
                run.getServer().getName(),
                run.getStartedAt(),
                results
        );
        return ResponseEntity.ok(response);
    }

    private TestResultResponse toResponse(TestResult tr) {
        AttackClassification c = tr.getClassificationResolved();
        return new TestResultResponse(
                tr.getId(),
                tr.getScenario().getName(),
                tr.getStatusCode(),
                c == AttackClassification.VULNERABLE,
                c.name(),
                tr.getReasonResolved(),
                tr.getSeverityResolved().name(),
                tr.getResponseBody()
        );
    }
}
