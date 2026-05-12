package com.fhir.security.service;

import com.fhir.security.attack.AttackClassification;
import com.fhir.security.dto.response.ServerAuthNarrativeResponse;
import com.fhir.security.entity.FhirServer;
import com.fhir.security.entity.TestResult;
import com.fhir.security.entity.TestRun;
import com.fhir.security.repository.TestRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ServerAuthNarrativeService {

    static final String SCENARIO_CROSS_PATIENT = "Cross-Patient Access";
    static final String SCENARIO_OPEN_ENDPOINT = "Open Endpoint Detection";
    static final String SCENARIO_TOKEN_ISOLATION = "Authenticated Token Isolation";
    static final String SCENARIO_OBS_BUNDLE = "Observation Bundle / Duplicate Clinical";

    private final FhirServerService fhirServerService;
    private final TestRunRepository testRunRepository;
    private final AuthEnvironmentProbe authEnvironmentProbe;
    private final AttackHttpClient attackHttpClient;

    public ServerAuthNarrativeService(
            FhirServerService fhirServerService,
            TestRunRepository testRunRepository,
            AuthEnvironmentProbe authEnvironmentProbe,
            AttackHttpClient attackHttpClient
    ) {
        this.fhirServerService = fhirServerService;
        this.testRunRepository = testRunRepository;
        this.authEnvironmentProbe = authEnvironmentProbe;
        this.attackHttpClient = attackHttpClient;
    }

    @Transactional(readOnly = true)
    public ServerAuthNarrativeResponse buildForServer(Long serverId) {
        FhirServer server = fhirServerService.getServerById(serverId);
        String base = server.getBaseUrl().replaceAll("/$", "");

        boolean oauth = authEnvironmentProbe.isOAuthAdvertised(base);
        int patientStatus = attackHttpClient.get(base + "/Patient?_count=1").statusCode();
        String envLabel = classifyAuthEnvironment(oauth, patientStatus);

        Optional<TestRun> latest = testRunRepository.findFirstByServer_IdOrderByStartedAtDesc(serverId);

        String crossCls = "";
        String crossReason = "";
        String openCls = "";
        String openReason = "";
        String tokCls = "";
        String tokReason = "";
        String bundleCls = "";
        String bundleReason = "";
        java.time.LocalDateTime runAt = null;

        if (latest.isPresent()) {
            TestRun run = latest.get();
            runAt = run.getStartedAt();
            crossCls = summarize(run, SCENARIO_CROSS_PATIENT, true);
            crossReason = summarize(run, SCENARIO_CROSS_PATIENT, false);
            openCls = summarize(run, SCENARIO_OPEN_ENDPOINT, true);
            openReason = summarize(run, SCENARIO_OPEN_ENDPOINT, false);
            tokCls = summarize(run, SCENARIO_TOKEN_ISOLATION, true);
            tokReason = summarize(run, SCENARIO_TOKEN_ISOLATION, false);
            bundleCls = summarize(run, SCENARIO_OBS_BUNDLE, true);
            bundleReason = summarize(run, SCENARIO_OBS_BUNDLE, false);
        }

        String narrative = buildNarrative(
                server.getName(),
                oauth,
                patientStatus,
                envLabel,
                crossCls,
                crossReason,
                openCls,
                openReason,
                tokCls,
                tokReason,
                bundleCls,
                bundleReason,
                latest.isPresent()
        );

        return new ServerAuthNarrativeResponse(
                server.getId(),
                server.getName(),
                server.getBaseUrl(),
                oauth,
                patientStatus,
                envLabel,
                runAt,
                crossCls,
                crossReason,
                openCls,
                openReason,
                tokCls,
                tokReason,
                bundleCls,
                bundleReason,
                narrative
        );
    }

    private static String classifyAuthEnvironment(boolean oauthAdvertised, int patientStatus) {
        if (oauthAdvertised) {
            if (patientStatus == 200 || patientStatus == 201) {
                return "OAuth/SMART advertised — anonymous Patient read succeeded (check policy vs metadata).";
            }
            if (patientStatus == 401 || patientStatus == 403) {
                return "OAuth/SMART advertised — anonymous Patient read rejected.";
            }
            return "OAuth/SMART advertised — anonymous Patient read returned HTTP " + patientStatus + ".";
        }
        if (patientStatus == 200 || patientStatus == 201) {
            return "No OAuth/SMART in structured metadata — anonymous Patient read succeeded (typical open sandbox).";
        }
        if (patientStatus == 401 || patientStatus == 403) {
            return "No OAuth/SMART in structured metadata — anonymous Patient read rejected.";
        }
        return "No OAuth/SMART in structured metadata — anonymous Patient read returned HTTP " + patientStatus + ".";
    }

    private static String summarize(TestRun run, String scenarioName, boolean classificationOnly) {
        return run.getTestResults().stream()
                .filter(tr -> tr.getScenario() != null && scenarioName.equals(tr.getScenario().getName()))
                .findFirst()
                .map(tr -> {
                    AttackClassification c = tr.getClassificationResolved();
                    String cls = c != null ? c.name() : "";
                    if (classificationOnly) {
                        return cls;
                    }
                    String r = tr.getReasonResolved();
                    return r.length() > 400 ? r.substring(0, 397) + "…" : r;
                })
                .orElse("");
    }

    private static String buildNarrative(
            String serverName,
            boolean oauth,
            int patientStatus,
            String envLabel,
            String crossCls,
            String crossReason,
            String openCls,
            String openReason,
            String tokCls,
            String tokReason,
            String bundleCls,
            String bundleReason,
            boolean hadRun
    ) {
        StringBuilder sb = new StringBuilder(900);
        sb.append("Server \"").append(serverName).append("\": ").append(envLabel);
        if (oauth) {
            sb.append(" Structured CapabilityStatement / SMART well-known indicates OAuth-style endpoints.");
        } else {
            sb.append(" No structured OAuth/SMART advertisement was detected from well-known + metadata probes.");
        }
        sb.append(" Live probe: GET /Patient?_count=1 returned HTTP ").append(patientStatus).append(".");

        if (!hadRun) {
            sb.append(" No completed test run is stored yet — run the attack suite to populate cross-patient, token isolation, and Observation bundle rows.");
            return sb.toString();
        }

        sb.append(" Latest stored run — Open-endpoint scenario: ").append(openCls.isEmpty() ? "n/a" : openCls);
        if (!openReason.isBlank()) {
            sb.append(" (").append(openReason).append(")");
        }
        sb.append(" Cross-patient scenario: ").append(crossCls.isEmpty() ? "n/a" : crossCls);
        if (!crossReason.isBlank()) {
            sb.append(" (").append(crossReason).append(")");
        }
        sb.append(" Authenticated token isolation: ").append(tokCls.isEmpty() ? "n/a" : tokCls);
        if (!tokReason.isBlank()) {
            sb.append(" (").append(tokReason).append(")");
        }
        sb.append(" Observation bundle / duplicate clinical: ").append(bundleCls.isEmpty() ? "n/a" : bundleCls);
        if (!bundleReason.isBlank()) {
            sb.append(" (").append(bundleReason).append(")");
        }
        return sb.toString();
    }
}
