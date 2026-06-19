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

        // Each probe gets a unique family name + identifier to prevent HAPI-2840 duplicate rejection.
        String marker1 = "inj-unknown-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String family1 = "InjProbe1-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        AttackHttpClient.HttpResult r1 = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\","
                        + "\"name\":[{\"family\":\"" + family1 + "\",\"given\":[\"" + marker1 + "\"]}],"
                        + "\"identifier\":[{\"system\":\"urn:probe\",\"value\":\"" + marker1 + "\"}],"
                        + "\"unknownField\":\"" + marker1 + "\","
                        + "\"__proto__\":{\"polluted\":\"" + marker1 + "\"}}"
        );
        AttackResult a1 = classifyInjectedPersistence(base, r1, marker1, "unknownField/__proto__ injection");

        // Duplicate key probe: use a unique family name; the injected duplicate id is the attack payload.
        String dupFamily = "DupKey-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String dupId = "dup-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        AttackHttpClient.HttpResult r2 = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\","
                        + "\"name\":[{\"family\":\"" + dupFamily + "\"}],"
                        + "\"id\":\"valid-" + dupId + "\","
                        + "\"id\":\"duplicate-" + dupId + "\"}"
        );
        AttackResult a2 = classifyDuplicateKeyProbe(base, r2, "duplicate-" + dupId);

        String marker3 = "inj-nested-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String family3 = "InjProbe3-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        AttackHttpClient.HttpResult r3 = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\","
                        + "\"name\":[{\"family\":\"" + family3 + "\",\"given\":[\"" + marker3 + "\"]}],"
                        + "\"identifier\":[{\"system\":\"urn:probe\",\"value\":\"" + marker3 + "\"}],"
                        + "\"_payload\":\"" + marker3 + "\","
                        + "\"extraNested\":{\"secret\":\"" + marker3 + "\"}}"
        );
        AttackResult a3 = classifyInjectedPersistence(base, r3, marker3, "_payload/extraNested injection");

        return AttackOutcome.combineWorstAll(a1, a2, a3);
    }

    private AttackResult classifyInjectedPersistence(String base, AttackHttpClient.HttpResult post, String marker, String label) {
        int code = post.statusCode();
        String body = post.responseBody();

        if (BehavioralProbeUtils.isDuplicateResourceError(body)) {
            return AttackOutcome.duplicateResourceInconclusive(code, body, label);
        }
        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body,
                    label + ": write rejected without authorization. Validation reached: NO. Rejection reason: authentication.");
        }
        if (code == 400 || code == 404 || code == 405 || code == 412 || code == 422) {
            return AttackOutcome.secure(code, body,
                    label + ": server rejected the payload. Validation reached: YES. Rejection reason: payload validation.");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body,
                    label + ": server error; cannot assess parsing/persistence. Validation reached: UNKNOWN.");
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

            // Detect persistence by checking for the malicious field NAMES in the GET response,
            // not the marker value. The marker also appears in legitimate fields (name.given,
            // identifier.value), so containsIgnoreCase(getBody, marker) always returns true and
            // would produce a false positive. Only the survival of the field names is meaningful.
            boolean persisted = BehavioralProbeUtils.containsIgnoreCase(getBody, "\"unknownField\"")
                    || BehavioralProbeUtils.containsIgnoreCase(getBody, "\"__proto__\"")
                    || BehavioralProbeUtils.containsIgnoreCase(getBody, "\"_payload\"")
                    || BehavioralProbeUtils.containsIgnoreCase(getBody, "\"extraNested\"");

            String combined = ""
                    + label + " POST (HTTP " + code + "):\n"
                    + AuthProbeUtils.truncate(body, 900)
                    + "\n\nFollow-up GET /Patient/{id} (HTTP " + get.statusCode() + "):\n"
                    + AuthProbeUtils.truncate(getBody, 1500);

            if (get.statusCode() == 401 || get.statusCode() == 403) {
                return AttackOutcome.inconclusive(
                        code,
                        combined,
                        label + ": follow-up GET requires authorization; cannot confirm persistence of injected fields. "
                                + "Validation reached: YES. Rejection reason: authorization on read."
                );
            }

            if (persisted) {
                return AttackOutcome.vulnerable(
                        code,
                        combined,
                        label + ": malicious field name(s) survived in GET response (unknownField / __proto__ / _payload / extraNested). "
                                + "Validation reached: YES. Rejection reason: none (payload accepted).",
                        AttackSeverity.MEDIUM
                );
            }

            return AttackOutcome.secure(
                    code,
                    combined,
                    label + ": malicious field names absent from GET response — server stripped unknown fields as expected. "
                            + "Validation reached: YES. Rejection reason: sanitization."
            );
        }

        return AttackOutcome.inconclusive(code, body,
                label + ": unexpected HTTP status: " + code + ". Validation reached: UNKNOWN.");
    }

    private AttackResult classifyDuplicateKeyProbe(String base, AttackHttpClient.HttpResult post, String duplicateIdValue) {
        int code = post.statusCode();
        String body = post.responseBody();

        if (BehavioralProbeUtils.isDuplicateResourceError(body)) {
            return AttackOutcome.duplicateResourceInconclusive(code, body, "duplicate key probe");
        }
        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body,
                    "Duplicate key: write rejected without authorization. Validation reached: NO. Rejection reason: authentication.");
        }
        if (code == 400 || code == 404 || code == 405 || code == 412 || code == 422) {
            return AttackOutcome.secure(code, body,
                    "Duplicate key: server rejected ambiguous JSON as expected. Validation reached: YES. Rejection reason: payload validation.");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body,
                    "Duplicate key: server error. Validation reached: UNKNOWN.");
        }

        if (code == 200 || code == 201) {
            String createdId = FhirResourceIdExtractor.extractId(body);
            if (createdId == null) {
                return AttackOutcome.inconclusive(code, body,
                        "Duplicate key: 2xx returned but no id; cannot verify behavior. Validation reached: YES.");
            }

            AttackHttpClient.HttpResult get = httpClient.get(base + "/Patient/" + createdId);
            String combined = ""
                    + "Duplicate key POST (HTTP " + code + "):\n"
                    + AuthProbeUtils.truncate(body, 900)
                    + "\n\nFollow-up GET /Patient/{id} (HTTP " + get.statusCode() + "):\n"
                    + AuthProbeUtils.truncate(get.responseBody(), 1500);

            if (BehavioralProbeUtils.containsIgnoreCase(body, duplicateIdValue)
                    || BehavioralProbeUtils.containsIgnoreCase(get.responseBody(), duplicateIdValue)) {
                return AttackOutcome.vulnerable(
                        code,
                        combined,
                        "Duplicate key: server accepted ambiguous JSON and reflected the last-seen id value. "
                                + "Validation reached: YES. Rejection reason: none (payload accepted).",
                        AttackSeverity.LOW
                );
            }

            return AttackOutcome.secure(
                    code,
                    combined,
                    "Duplicate key: request succeeded but server did not reflect the ambiguous client id; no clear persistence impact observed. "
                            + "Validation reached: YES. Rejection reason: sanitization."
            );
        }

        return AttackOutcome.inconclusive(code, body,
                "Duplicate key: unexpected HTTP status: " + code + ". Validation reached: UNKNOWN.");
    }
}
