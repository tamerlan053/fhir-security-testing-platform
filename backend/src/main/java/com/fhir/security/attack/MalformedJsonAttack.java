package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class MalformedJsonAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public MalformedJsonAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Malformed JSON Request";
    }

    @Override
    public String getDescription() {
        return "Sends truncated/invalid JSON to Patient endpoint to test server validation";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String malformed = "{ \"resourceType\": \"Patient\", ";
        String baseUrl = server.getBaseUrl().replaceAll("/$", "");
        String url = baseUrl + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, malformed);
        int statusCode = httpResult.statusCode();
        String responseBody = httpResult.responseBody();

        // 400 = correct validation; 200 or 500 = vulnerable (bad validation or server crash)
        boolean vulnerable = statusCode == 200 || statusCode == 500;

        return new AttackResult(statusCode, responseBody, vulnerable);
    }
}
