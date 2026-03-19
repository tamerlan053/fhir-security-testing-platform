package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class IncorrectResourceTypeAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public IncorrectResourceTypeAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Incorrect Resource Type";
    }

    @Override
    public String getDescription() {
        return "Sends JSON with fake resourceType to test if server validates resource type";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String payload = "{\"resourceType\":\"FakeResource\",\"id\":\"1\"}";
        String baseUrl = server.getBaseUrl().replaceAll("/$", "");
        String url = baseUrl + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        int statusCode = httpResult.statusCode();
        String responseBody = httpResult.responseBody();

        boolean vulnerable = statusCode == 200 || statusCode == 500;

        return new AttackResult(statusCode, responseBody, vulnerable);
    }
}
