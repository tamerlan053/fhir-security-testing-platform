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
        AttackResult r1 = classifyMalformedJsonProbe(base, truncated, "Truncated JSON");

        AttackHttpClient.HttpResult trailingComma = httpClient.post(url, "{ \"resourceType\": \"Patient\", }");
        AttackResult r2 = classifyMalformedJsonProbe(base, trailingComma, "Trailing comma JSON");

        return AttackOutcome.combineWorstAll(r1, r2);
    }

    private AttackResult classifyMalformedJsonProbe(String base, AttackHttpClient.HttpResult r, String label) {
        int code = r.statusCode();
        String body = r.responseBody();

        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body, label + ": request rejected; endpoint may require authentication for writes.");
        }
        if (code == 400 || code == 404 || code == 405 || code == 412 || code == 422) {
            return AttackOutcome.secure(code, body, label + ": server rejected malformed JSON as expected.");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body, label + ": server error; cannot determine parsing correctness.");
        }

        // Deep behavior: only call this vulnerable if malformed JSON led to an actual resource creation side effect.
        if (code == 200 || code == 201) {
            if (BehavioralProbeUtils.isOperationOutcome(body)) {
                return AttackOutcome.secure(code, body, label + ": OperationOutcome returned; server did not accept malformed JSON as a resource.");
            }

            String createdId = FhirResourceIdExtractor.extractId(body);
            if (createdId == null) {
                return AttackOutcome.inconclusive(
                        code,
                        body,
                        label + ": 2xx returned but no id in response; cannot verify whether a resource was created."
                );
            }

            AttackHttpClient.HttpResult get = httpClient.get(base + "/Patient/" + createdId);
            String combined = ""
                    + label + " POST (HTTP " + code + "):\n"
                    + AuthProbeUtils.truncate(body, 1000)
                    + "\n\nFollow-up GET /Patient/{id} (HTTP " + get.statusCode() + "):\n"
                    + AuthProbeUtils.truncate(get.responseBody(), 1400);

            if (get.statusCode() == 200 && !BehavioralProbeUtils.isOperationOutcome(get.responseBody())) {
                return AttackOutcome.vulnerable(
                        code,
                        combined,
                        label + ": malformed JSON resulted in a retrievable Patient resource (partial parsing / permissive parser).",
                        AttackSeverity.HIGH
                );
            }

            return AttackOutcome.inconclusive(
                    code,
                    combined,
                    label + ": 2xx returned but follow-up GET did not confirm a created resource."
            );
        }

        return AttackOutcome.inconclusive(code, body, label + ": unexpected HTTP status: " + code);
    }
}
