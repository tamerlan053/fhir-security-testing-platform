package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
        return "POST Patient with a custom extension marker and verify whether it persists on follow-up GET (covert channel check)";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String baseUrl = AuthProbeUtils.normalizeBase(server.getBaseUrl());
        String url = baseUrl + "/Patient";

        String marker = "covert-ext-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String payload = "{"
                + "\"resourceType\":\"Patient\","
                + "\"name\":[{\"family\":\"Probe-" + marker + "\"}],"
                + "\"extension\":[{"
                + "\"url\":\"http://malicious.example/hidden\","
                + "\"valueString\":\"" + marker + "\""
                + "}]"
                + "}";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        int code = httpResult.statusCode();
        String body = httpResult.responseBody();

        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body, "Write rejected without authorization.");
        }
        if (code == 400 || code == 404 || code == 405 || code == 412 || code == 422) {
            return AttackOutcome.secure(code, body, "Server rejected the payload (no covert channel persistence).");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body, "Server error during extension persistence probe.");
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

            AttackHttpClient.HttpResult get = httpClient.get(baseUrl + "/Patient/" + createdId);
            boolean persisted = BehavioralProbeUtils.containsIgnoreCase(get.responseBody(), marker);

            String combined = ""
                    + "POST /Patient (HTTP " + code + "):\n"
                    + AuthProbeUtils.truncate(body, 1000)
                    + "\n\nGET /Patient/{id} (HTTP " + get.statusCode() + "):\n"
                    + AuthProbeUtils.truncate(get.responseBody(), 1400);

            if (get.statusCode() == 401 || get.statusCode() == 403) {
                return AttackOutcome.inconclusive(
                        code,
                        combined,
                        "POST succeeded but follow-up GET requires authorization; cannot confirm whether marker persisted."
                );
            }
            if (persisted) {
                return AttackOutcome.vulnerable(
                        code,
                        combined,
                        "Custom extension marker persisted and is retrievable (potential covert storage channel).",
                        AttackSeverity.MEDIUM
                );
            }
            return AttackOutcome.secure(
                    code,
                    combined,
                    "Extension marker was not present on follow-up GET (server likely sanitized/ignored or did not persist it)."
            );
        }

        return AttackOutcome.inconclusive(code, body, "Unexpected status on extension persistence probe: " + code);
    }
}
