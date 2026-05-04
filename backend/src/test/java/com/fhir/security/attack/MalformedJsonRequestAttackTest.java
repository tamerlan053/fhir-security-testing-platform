package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MalformedJsonRequestAttackTest {

    @Test
    void execute_returnsSecure_whenMalformedPayloadsAreRejected() {
        AttackHttpClient httpClient = mock(AttackHttpClient.class);
        FhirServer server = new FhirServer("demo", "http://example.org/fhir/");
        MalformedJsonRequestAttack attack = new MalformedJsonRequestAttack(httpClient);

        when(httpClient.post(eq("http://example.org/fhir/Patient"), eq("{ \"resourceType\": \"Patient\", ")))
                .thenReturn(new AttackHttpClient.HttpResult(400, "{\"issue\":\"bad request\"}"));
        when(httpClient.post(eq("http://example.org/fhir/Patient"), eq("{ \"resourceType\": \"Patient\", }")))
                .thenReturn(new AttackHttpClient.HttpResult(401, "{\"issue\":\"unauthorized\"}"));

        AttackResult result = attack.execute(server);

        assertEquals(AttackClassification.SECURE, result.classification());
        assertEquals(400, result.statusCode());
        assertEquals(AttackSeverity.INFO, result.severity());
        assertTrue(result.reason().contains("server rejected malformed JSON as expected"));
        assertTrue(result.responseBody().contains("---"));
    }

    @Test
    void execute_returnsVulnerable_whenMalformedJsonCreatesRetrievableResource() {
        AttackHttpClient httpClient = mock(AttackHttpClient.class);
        FhirServer server = new FhirServer("demo", "http://example.org/fhir");
        MalformedJsonRequestAttack attack = new MalformedJsonRequestAttack(httpClient);

        when(httpClient.post(eq("http://example.org/fhir/Patient"), eq("{ \"resourceType\": \"Patient\", ")))
                .thenReturn(new AttackHttpClient.HttpResult(201, "{\"resourceType\":\"Patient\",\"id\":\"p-123\"}"));
        when(httpClient.get(eq("http://example.org/fhir/Patient/p-123")))
                .thenReturn(new AttackHttpClient.HttpResult(200, "{\"resourceType\":\"Patient\",\"id\":\"p-123\"}"));
        when(httpClient.post(eq("http://example.org/fhir/Patient"), eq("{ \"resourceType\": \"Patient\", }")))
                .thenReturn(new AttackHttpClient.HttpResult(400, "{\"issue\":\"bad request\"}"));

        AttackResult result = attack.execute(server);

        assertEquals(AttackClassification.VULNERABLE, result.classification());
        assertEquals(201, result.statusCode());
        assertEquals(AttackSeverity.HIGH, result.severity());
        assertTrue(result.reason().contains("malformed JSON resulted in a retrievable Patient resource"));
        verify(httpClient).get("http://example.org/fhir/Patient/p-123");
    }

    @Test
    void execute_returnsInconclusive_whenPostIs2xxWithoutResourceId() {
        AttackHttpClient httpClient = mock(AttackHttpClient.class);
        FhirServer server = new FhirServer("demo", "http://example.org/fhir");
        MalformedJsonRequestAttack attack = new MalformedJsonRequestAttack(httpClient);

        when(httpClient.post(eq("http://example.org/fhir/Patient"), eq("{ \"resourceType\": \"Patient\", ")))
                .thenReturn(new AttackHttpClient.HttpResult(201, "{\"resourceType\":\"Patient\"}"));
        when(httpClient.post(eq("http://example.org/fhir/Patient"), eq("{ \"resourceType\": \"Patient\", }")))
                .thenReturn(new AttackHttpClient.HttpResult(405, ""));

        AttackResult result = attack.execute(server);

        assertEquals(AttackClassification.INCONCLUSIVE, result.classification());
        assertEquals(201, result.statusCode());
        assertEquals(AttackSeverity.LOW, result.severity());
        assertTrue(result.reason().contains("2xx returned but no id in response"));
    }

    @Test
    void execute_returnsSecure_when2xxContainsOperationOutcome() {
        AttackHttpClient httpClient = mock(AttackHttpClient.class);
        FhirServer server = new FhirServer("demo", "http://example.org/fhir");
        MalformedJsonRequestAttack attack = new MalformedJsonRequestAttack(httpClient);

        when(httpClient.post(eq("http://example.org/fhir/Patient"), eq("{ \"resourceType\": \"Patient\", ")))
                .thenReturn(new AttackHttpClient.HttpResult(200, "{\"resourceType\":\"OperationOutcome\"}"));
        when(httpClient.post(eq("http://example.org/fhir/Patient"), eq("{ \"resourceType\": \"Patient\", }")))
                .thenReturn(new AttackHttpClient.HttpResult(400, "{\"resourceType\":\"OperationOutcome\"}"));

        AttackResult result = attack.execute(server);

        assertEquals(AttackClassification.SECURE, result.classification());
        assertEquals(200, result.statusCode());
        assertEquals(AttackSeverity.INFO, result.severity());
        assertTrue(result.reason().contains("OperationOutcome returned"));
    }
}
