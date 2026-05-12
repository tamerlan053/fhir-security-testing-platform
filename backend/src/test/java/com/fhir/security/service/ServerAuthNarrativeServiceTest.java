package com.fhir.security.service;

import com.fhir.security.dto.response.ServerAuthNarrativeResponse;
import com.fhir.security.entity.AttackScenario;
import com.fhir.security.entity.FhirServer;
import com.fhir.security.entity.TestResult;
import com.fhir.security.entity.TestRun;
import com.fhir.security.attack.AttackClassification;
import com.fhir.security.repository.TestRunRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServerAuthNarrativeServiceTest {

    @Test
    void narrative_mentions_no_run_when_repository_empty() {
        FhirServerService servers = mock(FhirServerService.class);
        TestRunRepository runs = mock(TestRunRepository.class);
        AuthEnvironmentProbe probe = mock(AuthEnvironmentProbe.class);
        AttackHttpClient http = mock(AttackHttpClient.class);

        FhirServer s = new FhirServer("Demo", "http://x/fhir");
        s.setId(7L);
        when(servers.getServerById(7L)).thenReturn(s);
        when(probe.isOAuthAdvertised("http://x/fhir")).thenReturn(false);
        when(http.get("http://x/fhir/Patient?_count=1")).thenReturn(new AttackHttpClient.HttpResult(200, "{}"));
        when(runs.findFirstByServer_IdOrderByStartedAtDesc(7L)).thenReturn(Optional.empty());

        ServerAuthNarrativeService svc = new ServerAuthNarrativeService(servers, runs, probe, http);
        ServerAuthNarrativeResponse r = svc.buildForServer(7L);

        assertEquals(7L, r.serverId());
        assertEquals(200, r.anonymousPatientReadHttpStatus());
        assertTrue(r.narrative().contains("No completed test run"));
    }

    @Test
    void narrative_includes_latest_classifications() {
        FhirServerService servers = mock(FhirServerService.class);
        TestRunRepository runs = mock(TestRunRepository.class);
        AuthEnvironmentProbe probe = mock(AuthEnvironmentProbe.class);
        AttackHttpClient http = mock(AttackHttpClient.class);

        FhirServer s = new FhirServer("Lab", "https://lab/fhir");
        s.setId(1L);
        when(servers.getServerById(1L)).thenReturn(s);
        when(probe.isOAuthAdvertised("https://lab/fhir")).thenReturn(true);
        when(http.get("https://lab/fhir/Patient?_count=1")).thenReturn(new AttackHttpClient.HttpResult(403, ""));

        AttackScenario scOpen = new AttackScenario(ServerAuthNarrativeService.SCENARIO_OPEN_ENDPOINT, "d", "M");
        AttackScenario scCross = new AttackScenario(ServerAuthNarrativeService.SCENARIO_CROSS_PATIENT, "d", "M");
        TestResult tr1 = new TestResult();
        tr1.setScenario(scOpen);
        tr1.setClassification(AttackClassification.SECURE);
        tr1.setReason("anon blocked");
        TestResult tr2 = new TestResult();
        tr2.setScenario(scCross);
        tr2.setClassification(AttackClassification.VULNERABLE);
        tr2.setReason("read ok");

        TestRun run = new TestRun(s, LocalDateTime.parse("2026-05-01T12:00:00"));
        run.setTestResults(List.of(tr1, tr2));

        when(runs.findFirstByServer_IdOrderByStartedAtDesc(1L)).thenReturn(Optional.of(run));

        ServerAuthNarrativeService svc = new ServerAuthNarrativeService(servers, runs, probe, http);
        ServerAuthNarrativeResponse r = svc.buildForServer(1L);

        assertEquals("SECURE", r.lastRunOpenEndpointClassification());
        assertEquals("VULNERABLE", r.lastRunCrossPatientClassification());
        assertTrue(r.narrative().contains("Cross-patient scenario: VULNERABLE"));
    }
}
