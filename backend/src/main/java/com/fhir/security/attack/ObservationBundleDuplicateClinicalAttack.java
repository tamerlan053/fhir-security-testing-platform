package com.fhir.security.attack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import com.fhir.security.service.AuthEnvironmentProbe;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Posts a transaction Bundle with multiple identical-structure Observation creates for the same subject
 * (duplicate clinical rows in one transaction) — tests whether extra clinical entries slip through policy.
 */
@Component
@Order(91)
public class ObservationBundleDuplicateClinicalAttack extends AbstractAccessControlAttack {

    private static final String LOINC = "718-7";

    private final AuthEnvironmentProbe authEnvironmentProbe;
    private final ObjectMapper mapper = new ObjectMapper();

    public ObservationBundleDuplicateClinicalAttack(AttackHttpClient httpClient, AuthEnvironmentProbe authEnvironmentProbe) {
        super(httpClient);
        this.authEnvironmentProbe = authEnvironmentProbe;
    }

    @Override
    public String getName() {
        return "Observation Bundle / Duplicate Clinical";
    }

    @Override
    public String getDescription() {
        return "Transaction Bundle: three POST Observation entries for the same Patient with the same LOINC code";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String base = baseUrl(server);
        boolean oauthAdvertised = authEnvironmentProbe.isOAuthAdvertised(base);

        CreateResult patient = createPatient(server, "BundleDup", "ClinicalSubject");
        if (patient.id() == null) {
            return AttackOutcome.setupCreateFailed(patient.statusCode(), patient.responseBody());
        }

        String pid = patient.id();
        String token = safeToken();
        String bundleJson = buildTransactionBundle(pid, token);
        AttackHttpClient.HttpResult post = httpClient.post(base, bundleJson);
        int code = post.statusCode();
        String body = post.responseBody() != null ? post.responseBody() : "";
        int createdCount = countCreatedEntries(body);

        String detail = "POST transaction Bundle (3 duplicate-structure Observations for Patient/" + pid + ") → HTTP "
                + code + ". Created-like entry responses: " + createdCount + ". "
                + AuthProbeUtils.truncate(body, 2000);

        if (code == 401 || code == 403) {
            return AttackOutcome.secure(code, detail, "Bundle transaction rejected without credentials.");
        }
        if (code == 400 || code == 404 || code == 405 || code == 422 || code == 412) {
            return AttackOutcome.secure(
                    code,
                    detail,
                    "Server rejected the duplicate-clinical bundle (expected integrity / validation behavior)."
            );
        }
        if (code != 200 && code != 201) {
            return AttackOutcome.inconclusive(code, detail, "Unexpected HTTP status on bundle transaction: " + code);
        }

        if (createdCount >= 3) {
            if (oauthAdvertised) {
                return AttackOutcome.vulnerable(
                        code,
                        detail,
                        "All three duplicate Observations were accepted in one transaction while OAuth/SMART is advertised (weak clinical duplication policy).",
                        AttackSeverity.MEDIUM
                );
            }
            return AttackOutcome.openPolicy(
                    code,
                    detail,
                    "All three duplicate Observations accepted in one transaction — common on open sandboxes without advertised OAuth."
            );
        }
        if (createdCount <= 0) {
            return AttackOutcome.inconclusive(
                    code,
                    detail,
                    "Bundle HTTP success but could not confirm Observation creates from response (partial rejection or non-standard response shape)."
            );
        }
        return AttackOutcome.inconclusive(
                code,
                detail,
                "Partial success: only " + createdCount + " of 3 Observation creates appear to have succeeded."
        );
    }

    private static String buildTransactionBundle(String patientId, String token) {
        String ref = "Patient/" + patientId;
        String obsCore = ""
                + "\"resourceType\":\"Observation\","
                + "\"status\":\"final\","
                + "\"code\":{\"coding\":[{\"system\":\"http://loinc.org\",\"code\":\"" + LOINC + "\"}],\"text\":\"Hemoglobin\"},"
                + "\"subject\":{\"reference\":\"" + escapeJson(ref) + "\"},"
                + "\"valueQuantity\":{\"value\":13.2,\"unit\":\"g/dL\",\"system\":\"http://unitsofmeasure.org\",\"code\":\"g/dL\"},"
                + "\"note\":[{\"text\":\"Week11-dup-" + escapeJson(token) + "\"}]";

        StringBuilder sb = new StringBuilder(800);
        sb.append("{\"resourceType\":\"Bundle\",\"type\":\"transaction\",\"entry\":[");
        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{")
                    .append("\"fullUrl\":\"urn:uuid:week11-dup-").append(i).append('-').append(token).append("\",")
                    .append("\"resource\":{").append(obsCore).append("},")
                    .append("\"request\":{\"method\":\"POST\",\"url\":\"Observation\"}")
                    .append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private int countCreatedEntries(String bundleResponse) {
        if (bundleResponse == null || bundleResponse.isBlank()) {
            return 0;
        }
        try {
            JsonNode root = mapper.readTree(bundleResponse);
            JsonNode entries = root.get("entry");
            if (entries == null || !entries.isArray()) {
                return 0;
            }
            int n = 0;
            for (JsonNode e : entries) {
                JsonNode resp = e.get("response");
                if (resp == null) {
                    continue;
                }
                JsonNode st = resp.get("status");
                if (st != null && st.isTextual()) {
                    String s = st.asText().trim();
                    if (s.startsWith("201")) {
                        n++;
                    }
                }
            }
            return n;
        } catch (Exception ignored) {
            return 0;
        }
    }
}
