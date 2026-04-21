package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Over-permissive parsing: unknown fields, duplicate keys, extra nested fragments.
 */
@Component
@Order(30)
public class UnexpectedPayloadInjectionAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public UnexpectedPayloadInjectionAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Unexpected Payload Injection";
    }

    @Override
    public String getDescription() {
        return "Unexpected properties, duplicate JSON keys, and hidden nested fragments on POST /Patient";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String base = AuthProbeUtils.normalizeBase(server.getBaseUrl());
        String url = base + "/Patient";

        AttackHttpClient.HttpResult r1 = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"Test\"}],"
                        + "\"unknownField\":\"should-reject\",\"__proto__\":{}}"
        );
        AttackResult a1 = AttackOutcome.validationPost(r1.statusCode(), r1.responseBody());

        AttackHttpClient.HttpResult r2 = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\",\"id\":\"valid-id\",\"id\":\"duplicate-id\"}"
        );
        AttackResult a2 = AttackOutcome.validationPost(r2.statusCode(), r2.responseBody());

        AttackHttpClient.HttpResult r3 = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"Test\"}],"
                        + "\"_payload\":\"hidden\",\"extraNested\":{\"secret\":\"data\"}}"
        );
        AttackResult a3 = AttackOutcome.validationPost(r3.statusCode(), r3.responseBody());

        return AttackOutcome.combineWorstAll(a1, a2, a3);
    }
}
