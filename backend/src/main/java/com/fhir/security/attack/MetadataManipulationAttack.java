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
        AttackResult a1 = classifyMetaVersionTypeProbe(rMeta);

        AttackHttpClient.HttpResult rType = httpClient.post(
                url,
                "{\"resourceType\":\"FakeResource\",\"id\":\"1\"}"
        );
        AttackResult a2 = classifyWrongResourceTypeProbe(rType);

        String idPayload = "visible\u0000hidden-data";
        AttackHttpClient.HttpResult rId = httpClient.post(
                url,
                "{\"resourceType\":\"Patient\",\"id\":\"" + idPayload + "\"}"
        );
        AttackResult a3 = classifyNullByteIdProbe(base, rId, idPayload);

        return AttackOutcome.combineWorstAll(a1, a2, a3);
    }

    private static AttackResult classifyMetaVersionTypeProbe(AttackHttpClient.HttpResult r) {
        int code = r.statusCode();
        String body = r.responseBody();

        // Strong secure signal: strict rejection of invalid meta type.
        if (code == 400 || code == 404 || code == 405 || code == 422 || code == 412) {
            return AttackOutcome.secure(code, body, "Server rejected invalid meta.versionId type as expected.");
        }

        // If the server created a resource, check whether it echoed the invalid numeric versionId.
        // Most servers will ignore client-supplied meta.versionId and assign their own; that's not a vulnerability.
        if (code == 200 || code == 201) {
            if (BehavioralProbeUtils.containsIgnoreCase(body, "\"versionId\":123")) {
                return AttackOutcome.vulnerable(
                        code,
                        body,
                        "Server echoed client-supplied numeric meta.versionId instead of ignoring/rejecting it.",
                        AttackSeverity.MEDIUM
                );
            }
            return AttackOutcome.secure(
                    code,
                    body,
                    "Server did not persist/echo the invalid meta.versionId (client meta appears ignored/sanitized)."
            );
        }

        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body, "Request rejected; endpoint may require authentication for writes.");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body, "Server error during meta.versionId type probe.");
        }
        return AttackOutcome.inconclusive(code, body, "Unexpected status on meta.versionId type probe: " + code);
    }

    private static AttackResult classifyWrongResourceTypeProbe(AttackHttpClient.HttpResult r) {
        int code = r.statusCode();
        String body = r.responseBody();

        if (code == 400 || code == 404 || code == 405 || code == 422 || code == 412) {
            return AttackOutcome.secure(code, body, "Server rejected invalid resourceType as expected.");
        }
        if (code == 200 || code == 201) {
            return AttackOutcome.vulnerable(
                    code,
                    body,
                    "Server accepted payload with incorrect resourceType (should reject).",
                    AttackSeverity.HIGH
            );
        }
        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body, "Request rejected; endpoint may require authentication for writes.");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body, "Server error during resourceType probe.");
        }
        return AttackOutcome.inconclusive(code, body, "Unexpected status on resourceType probe: " + code);
    }

    private AttackResult classifyNullByteIdProbe(String base, AttackHttpClient.HttpResult r, String sentId) {
        int code = r.statusCode();
        String body = r.responseBody();

        if (code == 400 || code == 404 || code == 405 || code == 422 || code == 412) {
            return AttackOutcome.secure(code, body, "Server rejected suspicious client-supplied id as expected.");
        }
        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, body, "Request rejected; endpoint may require authentication for writes.");
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, body, "Server error during id null-byte probe.");
        }

        if (code == 200 || code == 201) {
            // Behavior-based: vulnerable only if server appears to accept/reflect the hidden portion.
            boolean echoedHidden = BehavioralProbeUtils.containsIgnoreCase(body, "hidden-data")
                    || BehavioralProbeUtils.containsIgnoreCase(body, "\\u0000");
            String createdId = FhirResourceIdExtractor.extractId(body);

            // Follow-up GET using the server-assigned id. If the server sanitized/rewrote id, this should still work.
            String followUpBody = "";
            int followUpCode = 0;
            if (createdId != null) {
                AttackHttpClient.HttpResult get = httpClient.get(base + "/Patient/" + createdId);
                followUpCode = get.statusCode();
                followUpBody = get.responseBody() != null ? get.responseBody() : "";
                echoedHidden = echoedHidden
                        || BehavioralProbeUtils.containsIgnoreCase(followUpBody, "hidden-data")
                        || BehavioralProbeUtils.containsIgnoreCase(followUpBody, "\\u0000");
            }

            if (echoedHidden) {
                String detail = (body != null ? body : "")
                        + "\n\nFollow-up GET HTTP " + followUpCode + ":\n"
                        + AuthProbeUtils.truncate(followUpBody, 1200);
                return AttackOutcome.vulnerable(
                        code,
                        detail,
                        "Server accepted an id containing a null-byte marker and reflected/persisted the hidden portion.",
                        AttackSeverity.HIGH
                );
            }

            // If accepted but did not reflect/persist the marker, treat as secure-ish sanitization.
            String reason = "Server did not reflect/persist the null-byte marker (id appears sanitized/rewritten).";
            if (createdId != null && !createdId.equals(sentId)) {
                reason += " Assigned id: " + createdId + ".";
            }
            return AttackOutcome.secure(code, body, reason);
        }

        return AttackOutcome.inconclusive(code, body, "Unexpected status on id null-byte probe: " + code);
    }
}
