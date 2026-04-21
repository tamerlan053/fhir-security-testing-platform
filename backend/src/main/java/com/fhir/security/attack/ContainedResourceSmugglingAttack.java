package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Embedded contained resources (e.g. Binary) as a covert channel.
 */
@Component
@Order(50)
public class ContainedResourceSmugglingAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public ContainedResourceSmugglingAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Contained Resource Smuggling";
    }

    @Override
    public String getDescription() {
        return "POST Patient with embedded contained Binary (base64) to detect smuggling of nested payloads";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String payload = "{\"resourceType\":\"Patient\",\"contained\":[{\"resourceType\":\"Binary\",\"id\":\"covert\","
                + "\"contentType\":\"text/plain\",\"data\":\"c2VjcmV0LWRhdGE=\"}]}";
        String base = AuthProbeUtils.normalizeBase(server.getBaseUrl());
        String url = base + "/Patient";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        return AttackOutcome.validationPost(httpResult.statusCode(), httpResult.responseBody());
    }
}
