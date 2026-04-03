package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public abstract class AbstractAccessControlAttack implements ExecutableAttack {

    protected final AttackHttpClient httpClient;

    protected AbstractAccessControlAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    protected record CreateResult(String id, int statusCode, String responseBody) {
    }

    protected static boolean isVulnerableByStatus(int statusCode) {
        return statusCode == 200 || statusCode == 201 || statusCode == 500;
    }

    protected String baseUrl(FhirServer server) {
        return server.getBaseUrl().replaceAll("/$", "");
    }

    protected static String safeToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    protected static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    protected CreateResult createPatient(FhirServer server, String given, String family) {
        String token = safeToken();
        String payload = "{\"resourceType\":\"Patient\",\"name\":[{\"given\":[\"" + escapeJson(given + "-" + token) + "\"],\"family\":\"" + escapeJson(family + "-" + token) + "\"}]}";
        String url = baseUrl(server) + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        String id = FhirResourceIdExtractor.extractId(httpResult.responseBody());
        return new CreateResult(id, httpResult.statusCode(), httpResult.responseBody());
    }

    protected CreateResult createObservationForSubject(FhirServer server, String subjectPatientId, String value) {
        String token = safeToken();
        String safeSubjectRef = "Patient/" + subjectPatientId;
        String payload = "{"
                + "\"resourceType\":\"Observation\","
                + "\"status\":\"final\","
                + "\"code\":{\"text\":\"SecurityTest\",\"coding\":[{\"system\":\"http://loinc.org\",\"code\":\"718-7\"}]},"
                + "\"subject\":{\"reference\":\"" + escapeJson(safeSubjectRef) + "\"},"
                + "\"valueString\":\"" + escapeJson(value + "-" + token) + "\""
                + "}";

        String url = baseUrl(server) + "/Observation";
        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        String id = FhirResourceIdExtractor.extractId(httpResult.responseBody());
        return new CreateResult(id, httpResult.statusCode(), httpResult.responseBody());
    }

    protected String encodeQueryParam(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }
}

