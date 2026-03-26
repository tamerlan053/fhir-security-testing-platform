package com.fhir.security.service;

import com.fhir.security.dto.response.CompareAttackRowResponse;
import com.fhir.security.dto.response.CompareCellResponse;
import com.fhir.security.dto.response.CompareResponse;
import com.fhir.security.dto.response.CompareServerColumnResponse;
import com.fhir.security.entity.FhirServer;
import com.fhir.security.entity.TestResult;
import com.fhir.security.entity.TestRun;
import com.fhir.security.repository.FhirServerRepository;
import com.fhir.security.repository.TestRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class TestComparisonService {

    private static final int MAX_SERVERS = 15;

    private final FhirServerRepository fhirServerRepository;
    private final TestRunRepository testRunRepository;

    public TestComparisonService(FhirServerRepository fhirServerRepository,
                                TestRunRepository testRunRepository) {
        this.fhirServerRepository = fhirServerRepository;
        this.testRunRepository = testRunRepository;
    }

    @Transactional(readOnly = true)
    public CompareResponse compareRunsForServers(List<Long> serverIds) {
        if (serverIds == null || serverIds.isEmpty()) {
            throw new IllegalArgumentException("serverIds must contain at least one id");
        }
        List<Long> uniqueInOrder = serverIds.stream()
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        if (uniqueInOrder.size() > MAX_SERVERS) {
            throw new IllegalArgumentException("at most " + MAX_SERVERS + " servers allowed");
        }

        Map<Long, FhirServer> servers = new LinkedHashMap<>();
        Map<Long, TestRun> latestRunByServer = new LinkedHashMap<>();

        for (Long id : uniqueInOrder) {
            FhirServer server = fhirServerRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Server not found with id: " + id));
            servers.put(id, server);
            Optional<TestRun> latest = testRunRepository.findFirstByServer_IdOrderByStartedAtDesc(id);
            latestRunByServer.put(id, latest.orElse(null));
        }

        List<CompareServerColumnResponse> columns = new ArrayList<>();
        for (Long id : uniqueInOrder) {
            FhirServer s = servers.get(id);
            TestRun run = latestRunByServer.get(id);
            if (run == null) {
                columns.add(new CompareServerColumnResponse(
                        id, s.getName(), s.getBaseUrl(), null, null, 0, 0
                ));
            } else {
                List<TestResult> results = run.getTestResults();
                int vuln = (int) results.stream().filter(TestResult::isVulnerable).count();
                columns.add(new CompareServerColumnResponse(
                        id,
                        s.getName(),
                        s.getBaseUrl(),
                        run.getId(),
                        run.getStartedAt().toString(),
                        vuln,
                        results.size()
                ));
            }
        }

        Set<String> scenarioNames = new TreeSet<>();
        for (TestRun run : latestRunByServer.values()) {
            if (run == null) continue;
            for (TestResult tr : run.getTestResults()) {
                scenarioNames.add(tr.getScenario().getName());
            }
        }

        List<CompareAttackRowResponse> rows = new ArrayList<>();
        for (String scenarioName : scenarioNames) {
            List<CompareCellResponse> cells = new ArrayList<>();
            for (Long id : uniqueInOrder) {
                TestRun run = latestRunByServer.get(id);
                if (run == null) {
                    cells.add(new CompareCellResponse(id, false, null, null));
                    continue;
                }
                Optional<TestResult> match = run.getTestResults().stream()
                        .filter(tr -> scenarioName.equals(tr.getScenario().getName()))
                        .findFirst();
                if (match.isEmpty()) {
                    cells.add(new CompareCellResponse(id, false, null, null));
                } else {
                    TestResult tr = match.get();
                    cells.add(new CompareCellResponse(
                            id, true, tr.getStatusCode(), tr.isVulnerable()
                    ));
                }
            }
            rows.add(new CompareAttackRowResponse(scenarioName, cells));
        }

        return new CompareResponse(columns, rows);
    }
}
