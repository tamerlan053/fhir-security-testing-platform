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

        if (BehavioralProbeUtils.isDuplicateResourceError(body)) {
            return AttackOutcome.duplicateResourceInconclusive(code, body, "contained Binary smuggling probe");
        }
        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body,
                    "Contained Binary smuggling: write rejected without authorization. "
                            + "Validation reached: NO. Rejection reason: authentication.");
        }
        if (code == 400 || code == 404 || code == 405 || code == 412 || code == 422) {
            return AttackOutcome.secure(code, body,
                    "Contained Binary smuggling: server rejected the payload (no contained-resource persistence). "
                            + "Validation reached: YES. Rejection reason: payload validation.");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body,
                    "Contained Binary smuggling: server error. Validation reached: UNKNOWN.");
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
                        "Contained Binary smuggling: POST succeeded but follow-up GET requires authorization; cannot confirm persistence. "
                                + "Validation reached: YES. Rejection reason: authorization on read."
                );
            }

            if (persisted) {
                return AttackOutcome.vulnerable(
                        code,
                        combined,
                        "Contained Binary smuggling: marker persisted and is retrievable (potential nested payload smuggling channel). "
                                + "Validation reached: YES. Rejection reason: none (payload accepted).",
                        AttackSeverity.MEDIUM
                );
            }
            return AttackOutcome.secure(
                    code,
                    combined,
                    "Contained Binary smuggling: marker was not present on follow-up GET (server likely stripped/ignored contained content). "
                            + "Validation reached: YES. Rejection reason: sanitization."
            );
        }

        return AttackOutcome.inconclusive(code, body,
                "Contained Binary smuggling: unexpected status: " + code + ". Validation reached: UNKNOWN.");
    }
}
