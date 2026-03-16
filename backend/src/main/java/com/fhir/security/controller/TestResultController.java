package com.fhir.security.controller;

import com.fhir.security.dto.response.TestResultResponse;
import com.fhir.security.dto.response.TestRunResponse;
import com.fhir.security.entity.TestResult;
import com.fhir.security.entity.TestRun;
import com.fhir.security.exception.TestRunNotFoundException;
import com.fhir.security.repository.TestRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class TestResultController {

    private static final Logger log = LoggerFactory.getLogger(TestResultController.class);

    private final TestRunRepository testRunRepository;

    public TestResultController(TestRunRepository testRunRepository) {
        this.testRunRepository = testRunRepository;
    }

    @GetMapping("/{testRunId}")
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
        return new TestResultResponse(
                tr.getId(),
                tr.getScenario().getName(),
                tr.getStatusCode(),
                tr.isVulnerable(),
                tr.getResponseBody()
        );
    }
}
