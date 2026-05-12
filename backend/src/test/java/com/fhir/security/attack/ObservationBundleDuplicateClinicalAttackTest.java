package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import com.fhir.security.service.AuthEnvironmentProbe;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObservationBundleDuplicateClinicalAttackTest {

    @Test
    void open_policy_when_three_creates_and_no_oauth() {
        AttackHttpClient http = mock(AttackHttpClient.class);
        AuthEnvironmentProbe probe = mock(AuthEnvironmentProbe.class);
        when(probe.isOAuthAdvertised("http://hapi.example/fhir")).thenReturn(false);

        when(http.post(eq("http://hapi.example/fhir/Patient"), anyString()))
                .thenReturn(new AttackHttpClient.HttpResult(201, "{\"resourceType\":\"Patient\",\"id\":\"p42\"}"));

        String bundleResp = """
                {"resourceType":"Bundle","type":"transaction-response","entry":[
                  {"response":{"status":"201"}},
                  {"response":{"status":"201"}},
                  {"response":{"status":"201"}}
                ]}""";
        when(http.post(eq("http://hapi.example/fhir"), startsWith("{\"resourceType\":\"Bundle\"")))
                .thenReturn(new AttackHttpClient.HttpResult(200, bundleResp));

        ObservationBundleDuplicateClinicalAttack attack = new ObservationBundleDuplicateClinicalAttack(http, probe);
        AttackResult r = attack.execute(new FhirServer("h", "http://hapi.example/fhir"));

        assertEquals(AttackClassification.OPEN_POLICY, r.classification());
        assertTrue(r.reason().contains("three duplicate"));
    }

    @Test
    void vulnerable_when_three_creates_and_oauth_advertised() {
        AttackHttpClient http = mock(AttackHttpClient.class);
        AuthEnvironmentProbe probe = mock(AuthEnvironmentProbe.class);
        when(probe.isOAuthAdvertised("http://hapi.example/fhir")).thenReturn(true);

        when(http.post(eq("http://hapi.example/fhir/Patient"), anyString()))
                .thenReturn(new AttackHttpClient.HttpResult(201, "{\"resourceType\":\"Patient\",\"id\":\"p9\"}"));

        String bundleResp = """
                {"resourceType":"Bundle","entry":[
                  {"response":{"status":"201 Created"}},
                  {"response":{"status":"201"}},
                  {"response":{"status":"201"}}
                ]}""";
        when(http.post(eq("http://hapi.example/fhir"), startsWith("{\"resourceType\":\"Bundle\"")))
                .thenReturn(new AttackHttpClient.HttpResult(200, bundleResp));

        ObservationBundleDuplicateClinicalAttack attack = new ObservationBundleDuplicateClinicalAttack(http, probe);
        AttackResult r = attack.execute(new FhirServer("h", "http://hapi.example/fhir"));

        assertEquals(AttackClassification.VULNERABLE, r.classification());
        assertEquals(AttackSeverity.MEDIUM, r.severity());
    }

    @Test
    void secure_when_bundle_rejected() {
        AttackHttpClient http = mock(AttackHttpClient.class);
        AuthEnvironmentProbe probe = mock(AuthEnvironmentProbe.class);
        when(probe.isOAuthAdvertised("http://hapi.example/fhir")).thenReturn(false);

        when(http.post(eq("http://hapi.example/fhir/Patient"), anyString()))
                .thenReturn(new AttackHttpClient.HttpResult(201, "{\"resourceType\":\"Patient\",\"id\":\"p1\"}"));

        when(http.post(eq("http://hapi.example/fhir"), startsWith("{\"resourceType\":\"Bundle\"")))
                .thenReturn(new AttackHttpClient.HttpResult(400, "{\"resourceType\":\"OperationOutcome\"}"));

        ObservationBundleDuplicateClinicalAttack attack = new ObservationBundleDuplicateClinicalAttack(http, probe);
        AttackResult r = attack.execute(new FhirServer("h", "http://hapi.example/fhir"));

        assertEquals(AttackClassification.SECURE, r.classification());
    }
}
