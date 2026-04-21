package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(40)
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
        String baseUrl = AuthProbeUtils.normalizeBase(server.getBaseUrl());
        String url = baseUrl + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        return AttackOutcome.validationPost(httpResult.statusCode(), httpResult.responseBody());
    }
}
