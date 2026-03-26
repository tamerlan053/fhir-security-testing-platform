package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class ManipulatedIdentifiersAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public ManipulatedIdentifiersAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Manipulated Identifiers";
    }

    @Override
    public String getDescription() {
        return "Sends Patient with null byte in id and suspicious identifier value to test covert identifier channels";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String payload = "{\"resourceType\":\"Patient\",\"id\":\"visible\u0000hidden-data\",\"identifier\":[{\"system\":\"urn:test\",\"value\":\"id;secret=1\"}]}";
        String baseUrl = server.getBaseUrl().replaceAll("/$", "");
        String url = baseUrl + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        int statusCode = httpResult.statusCode();
        String responseBody = httpResult.responseBody();

        boolean vulnerable = statusCode == 200 || statusCode == 201 || statusCode == 500;

        return new AttackResult(statusCode, responseBody, vulnerable);
    }
}
