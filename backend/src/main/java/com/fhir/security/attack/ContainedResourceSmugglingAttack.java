package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

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
        return "POST Patient with contained Binary marker and verify whether it persists on follow-up GET (covert channel check)";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String base = AuthProbeUtils.normalizeBase(server.getBaseUrl());
        String url = base + "/Patient";

        String marker = "covert-bin-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String b64 = Base64.getEncoder().encodeToString(marker.getBytes(StandardCharsets.UTF_8));
        String payload = "{"
                + "\"resourceType\":\"Patient\","
                + "\"name\":[{\"family\":\"Probe-" + marker + "\"}],"
                + "\"contained\":[{"
                + "\"resourceType\":\"Binary\","
                + "\"id\":\"covert\","
                + "\"contentType\":\"text/plain\","
                + "\"data\":\"" + b64 + "\""
                + "}]"
                + "}";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        int code = httpResult.statusCode();
        String body = httpResult.responseBody();

        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body, "Write rejected without authorization.");
        }
        if (code == 400 || code == 404 || code == 405 || code == 412 || code == 422) {
            return AttackOutcome.secure(code, body, "Server rejected the payload (no contained-resource persistence).");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body, "Server error during contained Binary persistence probe.");
        }

        if (code == 200 || code == 201) {
            String createdId = FhirResourceIdExtractor.extractId(body);
            if (createdId == null) {
                return AttackOutcome.inconclusive(
                        code,
                        body,
                        "POST succeeded but response did not include a resource id; cannot verify persistence."
                );
            }

            AttackHttpClient.HttpResult get = httpClient.get(base + "/Patient/" + createdId);
            boolean persisted = BehavioralProbeUtils.containsIgnoreCase(get.responseBody(), b64)
                    || BehavioralProbeUtils.containsIgnoreCase(get.responseBody(), marker);

            String combined = ""
                    + "POST /Patient (HTTP " + code + "):\n"
                    + AuthProbeUtils.truncate(body, 1000)
                    + "\n\nGET /Patient/{id} (HTTP " + get.statusCode() + "):\n"
                    + AuthProbeUtils.truncate(get.responseBody(), 1400);

            if (get.statusCode() == 401 || get.statusCode() == 403) {
                return AttackOutcome.inconclusive(
                        code,
                        combined,
                        "POST succeeded but follow-up GET requires authorization; cannot confirm whether contained Binary persisted."
                );
            }

            if (persisted) {
                return AttackOutcome.vulnerable(
                        code,
                        combined,
                        "Contained Binary marker persisted and is retrievable (potential nested payload smuggling channel).",
                        AttackSeverity.MEDIUM
                );
            }
            return AttackOutcome.secure(
                    code,
                    combined,
                    "Contained Binary marker was not present on follow-up GET (server likely stripped/ignored contained content)."
            );
        }

        return AttackOutcome.inconclusive(code, body, "Unexpected status on contained Binary persistence probe: " + code);
    }
}
