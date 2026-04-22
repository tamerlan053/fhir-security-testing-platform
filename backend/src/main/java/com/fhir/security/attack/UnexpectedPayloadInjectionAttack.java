package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

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

        String marker1 = "inj-unknown-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        AttackHttpClient.HttpResult r1 = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"Test\"}],"
                        + "\"unknownField\":\"" + marker1 + "\",\"__proto__\":{\"polluted\":\"" + marker1 + "\"}}"
        );
        AttackResult a1 = classifyInjectedPersistence(base, r1, marker1, "unknownField/__proto__ injection");

        AttackHttpClient.HttpResult r2 = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\",\"id\":\"valid-id\",\"id\":\"duplicate-id\"}"
        );
        AttackResult a2 = classifyDuplicateKeyProbe(base, r2);

        String marker3 = "inj-nested-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        AttackHttpClient.HttpResult r3 = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\",\"name\":[{\"family\":\"Test\"}],"
                        + "\"_payload\":\"" + marker3 + "\",\"extraNested\":{\"secret\":\"" + marker3 + "\"}}"
        );
        AttackResult a3 = classifyInjectedPersistence(base, r3, marker3, "_payload/extraNested injection");

        return AttackOutcome.combineWorstAll(a1, a2, a3);
    }

    private AttackResult classifyInjectedPersistence(String base, AttackHttpClient.HttpResult post, String marker, String label) {
        int code = post.statusCode();
        String body = post.responseBody();

        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body, label + ": write rejected without authorization.");
        }
        if (code == 400 || code == 404 || code == 405 || code == 412 || code == 422) {
            return AttackOutcome.secure(code, body, label + ": server rejected the payload.");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body, label + ": server error; cannot assess parsing/persistence.");
        }

        if (code == 200 || code == 201) {
            String createdId = FhirResourceIdExtractor.extractId(body);
            if (createdId == null) {
                return AttackOutcome.inconclusive(
                        code,
                        body,
                        label + ": 2xx returned but no id in response; cannot verify persistence."
                );
            }

            AttackHttpClient.HttpResult get = httpClient.get(base + "/Patient/" + createdId);
            String getBody = get.responseBody();
            boolean persisted = BehavioralProbeUtils.containsIgnoreCase(getBody, marker);

            String combined = ""
                    + label + " POST (HTTP " + code + "):\n"
                    + AuthProbeUtils.truncate(body, 900)
                    + "\n\nFollow-up GET /Patient/{id} (HTTP " + get.statusCode() + "):\n"
                    + AuthProbeUtils.truncate(getBody, 1500);

            if (get.statusCode() == 401 || get.statusCode() == 403) {
                return AttackOutcome.inconclusive(
                        code,
                        combined,
                        label + ": follow-up GET requires authorization; cannot confirm persistence of injected fields."
                );
            }

            if (persisted) {
                return AttackOutcome.vulnerable(
                        code,
                        combined,
                        label + ": injected marker persisted and is retrievable (over-permissive parsing/persistence).",
                        AttackSeverity.MEDIUM
                );
            }

            return AttackOutcome.secure(
                    code,
                    combined,
                    label + ": marker not present on follow-up GET (server likely ignored/stripped unexpected fields)."
            );
        }

        return AttackOutcome.inconclusive(code, body, label + ": unexpected HTTP status: " + code);
    }

    private AttackResult classifyDuplicateKeyProbe(String base, AttackHttpClient.HttpResult post) {
        int code = post.statusCode();
        String body = post.responseBody();

        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body, "Duplicate key: write rejected without authorization.");
        }
        if (code == 400 || code == 404 || code == 405 || code == 412 || code == 422) {
            return AttackOutcome.secure(code, body, "Duplicate key: server rejected ambiguous JSON as expected.");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body, "Duplicate key: server error.");
        }

        if (code == 200 || code == 201) {
            // This is an ambiguity/protocol-hardening check. Treat as low severity unless we can show persistence effects.
            String createdId = FhirResourceIdExtractor.extractId(body);
            if (createdId == null) {
                return AttackOutcome.inconclusive(code, body, "Duplicate key: 2xx returned but no id; cannot verify behavior.");
            }

            AttackHttpClient.HttpResult get = httpClient.get(base + "/Patient/" + createdId);
            String combined = ""
                    + "Duplicate key POST (HTTP " + code + "):\n"
                    + AuthProbeUtils.truncate(body, 900)
                    + "\n\nFollow-up GET /Patient/{id} (HTTP " + get.statusCode() + "):\n"
                    + AuthProbeUtils.truncate(get.responseBody(), 1500);

            if (BehavioralProbeUtils.containsIgnoreCase(body, "\"id\":\"duplicate-id\"")
                    || BehavioralProbeUtils.containsIgnoreCase(get.responseBody(), "\"id\":\"duplicate-id\"")) {
                return AttackOutcome.vulnerable(
                        code,
                        combined,
                        "Duplicate key: server accepted ambiguous JSON and reflected the last-seen id value.",
                        AttackSeverity.LOW
                );
            }

            return AttackOutcome.secure(
                    code,
                    combined,
                    "Duplicate key: request succeeded but server did not reflect the ambiguous client id; no clear persistence impact observed."
            );
        }

        return AttackOutcome.inconclusive(code, body, "Duplicate key: unexpected HTTP status: " + code);
    }
}
