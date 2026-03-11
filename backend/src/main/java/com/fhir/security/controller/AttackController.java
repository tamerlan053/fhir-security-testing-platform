package com.fhir.security.controller;

import com.fhir.security.entity.TestRun;
import com.fhir.security.service.AttackExecutorService;
import com.fhir.security.service.FhirServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attacks")
public class AttackController {

    private static final Logger log = LoggerFactory.getLogger(AttackController.class);

    private final AttackExecutorService attackExecutorService;
    private final FhirServerService fhirServerService;

    public AttackController(AttackExecutorService attackExecutorService, FhirServerService fhirServerService) {
        this.attackExecutorService = attackExecutorService;
        this.fhirServerService = fhirServerService;
    }

    @PostMapping("/run/{serverId}")
    public ResponseEntity<RunResult> runAttacks(@PathVariable Long serverId) {
        log.info("POST /api/attacks/run/{} - starting attack run", serverId);
        var server = fhirServerService.getServerById(serverId);
        TestRun run = attackExecutorService.executeAll(server);
        return ResponseEntity.ok(new RunResult(run.getId(), run.getStartedAt().toString()));
    }

    public record RunResult(Long testRunId, String startedAt) {}
}
