package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class InvalidJsonStructureAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public InvalidJsonStructureAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Invalid Json Structure";
    }

    @Override
    public String getDescription() {
        return "Sends JSON with trailing comma (invalid per RFC 8259) to test server validation";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String invalidJson = "{ \"resourceType\": \"Patient\", }";
        String baseUrl = server.getBaseUrl().replaceAll("/$", "");
        String url = baseUrl + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, invalidJson);
        int statusCode = httpResult.statusCode();
        String responseBody = httpResult.responseBody();

        boolean vulnerable = statusCode == 200 || statusCode == 201 || statusCode == 500;

        return new AttackResult(statusCode, responseBody, vulnerable);
    }
}
