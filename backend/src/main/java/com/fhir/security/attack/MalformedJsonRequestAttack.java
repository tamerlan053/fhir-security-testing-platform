package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Consolidated syntactically invalid JSON probes (truncated body + invalid JSON syntax).
 */
@Component
@Order(10)
public class MalformedJsonRequestAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public MalformedJsonRequestAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Malformed JSON Request";
    }

    @Override
    public String getDescription() {
        return "Truncated JSON and invalid JSON syntax on POST /Patient to test rejection of malformed payloads";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String base = AuthProbeUtils.normalizeBase(server.getBaseUrl());
        String url = base + "/Patient";

        AttackHttpClient.HttpResult truncated = httpClient.post(url, "{ \"resourceType\": \"Patient\", ");
        AttackResult r1 = AttackOutcome.validationPost(truncated.statusCode(), truncated.responseBody());

        AttackHttpClient.HttpResult trailingComma = httpClient.post(url, "{ \"resourceType\": \"Patient\", }");
        AttackResult r2 = AttackOutcome.validationPost(trailingComma.statusCode(), trailingComma.responseBody());

        return AttackOutcome.combineWorstAll(r1, r2);
    }
}
