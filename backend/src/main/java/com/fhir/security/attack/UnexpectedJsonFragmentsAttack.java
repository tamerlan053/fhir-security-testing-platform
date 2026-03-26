package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class UnexpectedJsonFragmentsAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public UnexpectedJsonFragmentsAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Unexpected JSON Fragments";
    }

    @Override
    public String getDescription() {
        return "Sends Patient with extra JSON properties (_payload, nested) to test acceptance of hidden structure";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String payload = "{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"Test\"}],\"_payload\":\"hidden\",\"extraNested\":{\"secret\":\"data\"}}";
        String baseUrl = server.getBaseUrl().replaceAll("/$", "");
        String url = baseUrl + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        int statusCode = httpResult.statusCode();
        String responseBody = httpResult.responseBody();

        boolean vulnerable = statusCode == 200 || statusCode == 201 || statusCode == 500;

        return new AttackResult(statusCode, responseBody, vulnerable);
    }
}
