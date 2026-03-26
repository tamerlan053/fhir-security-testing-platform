package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.stereotype.Component;

@Component
public class ExtensionFieldsMisuseAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public ExtensionFieldsMisuseAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Extension Fields Misuse";
    }

    @Override
    public String getDescription() {
        return "Sends Patient with custom extension containing hidden payload to test if server rejects or stores covert data";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String payload = "{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"Test\"}],\"extension\":[{\"url\":\"http://malicious.example/hidden\",\"valueString\":\"covert-payload\"}]}";
        String baseUrl = server.getBaseUrl().replaceAll("/$", "");
        String url = baseUrl + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        int statusCode = httpResult.statusCode();
        String responseBody = httpResult.responseBody();

        boolean vulnerable = statusCode == 200 || statusCode == 201 || statusCode == 500;

        return new AttackResult(statusCode, responseBody, vulnerable);
    }
}
