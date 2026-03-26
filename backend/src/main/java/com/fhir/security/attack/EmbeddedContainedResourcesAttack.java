package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class EmbeddedContainedResourcesAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public EmbeddedContainedResourcesAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Embedded Contained Resources";
    }

    @Override
    public String getDescription() {
        return "Sends Patient with embedded Binary containing base64 payload to test covert contained-resource channel";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String payload = "{\"resourceType\":\"Patient\",\"contained\":[{\"resourceType\":\"Binary\",\"id\":\"covert\",\"contentType\":\"text/plain\",\"data\":\"c2VjcmV0LWRhdGE=\"}]}";
        String baseUrl = server.getBaseUrl().replaceAll("/$", "");
        String url = baseUrl + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        int statusCode = httpResult.statusCode();
        String responseBody = httpResult.responseBody();

        boolean vulnerable = statusCode == 200 || statusCode == 201 || statusCode == 500;

        return new AttackResult(statusCode, responseBody, vulnerable);
    }
}
