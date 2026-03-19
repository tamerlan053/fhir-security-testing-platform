package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class UnexpectedFieldsAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public UnexpectedFieldsAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Unexpected Fields";
    }

    @Override
    public String getDescription() {
        return "Sends JSON with non-FHIR fields to test if server rejects unknown properties";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String payload = "{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"Test\"}],\"unknownField\":\"should-reject\",\"__proto__\":{}}";
        String baseUrl = server.getBaseUrl().replaceAll("/$", "");
        String url = baseUrl + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        int statusCode = httpResult.statusCode();
        String responseBody = httpResult.responseBody();

        boolean vulnerable = statusCode == 200 || statusCode == 500;

        return new AttackResult(statusCode, responseBody, vulnerable);
    }
}
