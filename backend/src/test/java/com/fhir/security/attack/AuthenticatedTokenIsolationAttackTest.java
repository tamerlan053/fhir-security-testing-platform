package com.fhir.security.attack;

import com.fhir.security.config.FhirSecurityTestProperties;
import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import com.fhir.security.service.AuthEnvironmentProbe;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthenticatedTokenIsolationAttackTest {

    @Test
    void na_when_no_token_configured() {
        AttackHttpClient http = mock(AttackHttpClient.class);
        AuthEnvironmentProbe probe = mock(AuthEnvironmentProbe.class);
        FhirSecurityTestProperties props = new FhirSecurityTestProperties();
        props.setBearerToken("");

        AuthenticatedTokenIsolationAttack attack = new AuthenticatedTokenIsolationAttack(http, probe, props);
        FhirServer server = new FhirServer("s", "https://lab.example/fhir");

        AttackResult r = attack.execute(server);

        assertEquals(AttackClassification.INCONCLUSIVE, r.classification());
        assertTrue(r.reason().contains("N/A"));
        verifyNoInteractions(http);
    }

    @Test
    void na_when_token_but_no_patient_id() {
        AttackHttpClient http = mock(AttackHttpClient.class);
        AuthEnvironmentProbe probe = mock(AuthEnvironmentProbe.class);
        FhirSecurityTestProperties props = new FhirSecurityTestProperties();
        props.setBearerToken("secret-token");
        props.setOutOfScopePatientId("  ");

        AuthenticatedTokenIsolationAttack attack = new AuthenticatedTokenIsolationAttack(http, probe, props);
        AttackResult r = attack.execute(new FhirServer("s", "https://lab.example/fhir"));

        assertTrue(r.reason().contains("out-of-scope patient id"));
        verifyNoInteractions(http);
    }

    @Test
    void na_when_lab_filter_mismatch() {
        AttackHttpClient http = mock(AttackHttpClient.class);
        AuthEnvironmentProbe probe = mock(AuthEnvironmentProbe.class);
        FhirSecurityTestProperties props = new FhirSecurityTestProperties();
        props.setBearerToken("t");
        props.setOutOfScopePatientId("99");
        props.setLabBaseUrlContains("other-host");

        AuthenticatedTokenIsolationAttack attack = new AuthenticatedTokenIsolationAttack(http, probe, props);
        AttackResult r = attack.execute(new FhirServer("s", "https://lab.example/fhir"));

        assertTrue(r.reason().contains("does not match"));
        verifyNoInteractions(http);
    }

    @Test
    void probes_when_configured_and_filter_matches() {
        AttackHttpClient http = mock(AttackHttpClient.class);
        AuthEnvironmentProbe probe = mock(AuthEnvironmentProbe.class);
        when(probe.isOAuthAdvertised("https://lab.example/fhir")).thenReturn(true);

        FhirSecurityTestProperties props = new FhirSecurityTestProperties();
        props.setBearerToken("mytoken");
        props.setOutOfScopePatientId("555");
        props.setLabBaseUrlContains("lab.example");

        when(http.get(eq("https://lab.example/fhir/Patient/555"), any(HttpHeaders.class)))
                .thenReturn(new AttackHttpClient.HttpResult(403, "no"));

        AuthenticatedTokenIsolationAttack attack = new AuthenticatedTokenIsolationAttack(http, probe, props);
        AttackResult r = attack.execute(new FhirServer("s", "https://lab.example/fhir"));

        assertEquals(AttackClassification.SECURE, r.classification());
    }
}
