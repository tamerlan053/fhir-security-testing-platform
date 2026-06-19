package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Order(60)
public class EncodedHiddenDataAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;

    public EncodedHiddenDataAttack(AttackHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getName() {
        return "Encoded Hidden Data";
    }

    @Override
    public String getDescription() {
        return "POST Patient meta.tag display marker (unicode-escaped) and verify persistence/normalization on follow-up GET (covert channel check)";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String baseUrl = AuthProbeUtils.normalizeBase(server.getBaseUrl());
        String url = baseUrl + "/Patient";

        String marker = "covert-tag-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        // JSON unicode escaping is just encoding; we treat vulnerability only if marker persists and is retrievable.
        String escaped = marker
                .chars()
                .collect(StringBuilder::new, (sb, ch) -> sb.append(String.format("\\u%04x", ch)), StringBuilder::append)
                .toString();

        String payload = "{"
                + "\"resourceType\":\"Patient\","
                + "\"name\":[{\"family\":\"TagProbe-" + marker + "\"}],"
                + "\"identifier\":[{\"system\":\"urn:probe\",\"value\":\"" + marker + "\"}],"
                + "\"meta\":{\"tag\":[{\"code\":\"x\",\"display\":\"" + escaped + "\"}]}"
                + "}";

        AttackHttpClient.HttpResult httpResult = httpClient.post(url, payload);
        int code = httpResult.statusCode();
        String body = httpResult.responseBody();

        if (BehavioralProbeUtils.isDuplicateResourceError(body)) {
            return AttackOutcome.duplicateResourceInconclusive(code, body, "encoded hidden data probe");
        }
        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body,
                    "Encoded hidden data: write rejected without authorization. "
                            + "Validation reached: NO. Rejection reason: authentication.");
        }
        if (code == 400 || code == 404 || code == 405 || code == 412 || code == 422) {
            return AttackOutcome.secure(code, body,
                    "Encoded hidden data: server rejected the payload (no encoded marker persistence). "
                            + "Validation reached: YES. Rejection reason: payload validation.");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body,
                    "Encoded hidden data: server error. Validation reached: UNKNOWN.");
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
            String getBody = get.responseBody();

            boolean persisted = BehavioralProbeUtils.containsIgnoreCase(getBody, marker)
                    || BehavioralProbeUtils.containsIgnoreCase(getBody, escaped);

            String combined = ""
                    + "POST /Patient (HTTP " + code + "):\n"
                    + AuthProbeUtils.truncate(body, 1000)
                    + "\n\nGET /Patient/{id} (HTTP " + get.statusCode() + "):\n"
                    + AuthProbeUtils.truncate(getBody, 1400);

            if (get.statusCode() == 401 || get.statusCode() == 403) {
                return AttackOutcome.inconclusive(
                        code,
                        combined,
                        "Encoded hidden data: POST succeeded but follow-up GET requires authorization; cannot confirm persistence. "
                                + "Validation reached: YES. Rejection reason: authorization on read."
                );
            }

            if (persisted) {
                return AttackOutcome.vulnerable(
                        code,
                        combined,
                        "Encoded hidden data: meta.tag display marker persisted and is retrievable (potential encoded covert storage channel). "
                                + "Validation reached: YES. Rejection reason: none (payload accepted).",
                        AttackSeverity.LOW
                );
            }

            return AttackOutcome.secure(
                    code,
                    combined,
                    "Encoded hidden data: meta.tag marker was not present on follow-up GET (server likely normalized/stripped it). "
                            + "Validation reached: YES. Rejection reason: sanitization."
            );
        }

        return AttackOutcome.inconclusive(code, body,
                "Encoded hidden data: unexpected status: " + code + ". Validation reached: UNKNOWN.");
    }
}
