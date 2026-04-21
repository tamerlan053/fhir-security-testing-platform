package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Weak semantic validation: bad meta, wrong resourceType, suspicious identifiers.
 */
@Component
@Order(20)
public class MetadataManipulationAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public MetadataManipulationAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Metadata Manipulation";
    }

    @Override
    public String getDescription() {
        return "Invalid meta.versionId type, fake resourceType, and manipulated identifiers on POST /Patient";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String base = AuthProbeUtils.normalizeBase(server.getBaseUrl());
        String url = base + "/Patient";

        AttackHttpClient.HttpResult rMeta = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\",\"meta\":{\"versionId\":123}}"
        );
        AttackResult a1 = AttackOutcome.validationPost(rMeta.statusCode(), rMeta.responseBody());

        AttackHttpClient.HttpResult rType = httpClient.post(
                url,
                "{\"resourceType\":\"FakeResource\",\"id\":\"1\"}"
        );
        AttackResult a2 = AttackOutcome.validationPost(rType.statusCode(), rType.responseBody());

        AttackHttpClient.HttpResult rId = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\",\"id\":\"visible\u0000hidden-data\","
                        + "\"identifier\":[{\"system\":\"urn:test\",\"value\":\"id;secret=1\"}]}"
        );
        AttackResult a3 = AttackOutcome.validationPost(rId.statusCode(), rId.responseBody());

        return AttackOutcome.combineWorstAll(a1, a2, a3);
    }
}
