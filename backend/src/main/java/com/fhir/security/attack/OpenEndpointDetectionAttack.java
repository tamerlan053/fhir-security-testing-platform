package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import com.fhir.security.service.AuthEndpointSupport;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Compares advertised SMART/OAuth with unauthenticated Patient read.
 */
@Component
@Order(80)
public class OpenEndpointDetectionAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;
    private final AuthEndpointSupport authEndpointSupport;

    public OpenEndpointDetectionAttack(AttackHttpClient httpClient, AuthEndpointSupport authEndpointSupport) {
        this.httpClient = httpClient;
        this.authEndpointSupport = authEndpointSupport;
    }

    @Override
    public String getName() {
        return "Open Endpoint Detection";
    }

    @Override
    public String getDescription() {
        return "Compares advertised SMART/OAuth with unauthenticated GET /Patient (Week 7)";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String base = AuthProbeUtils.normalizeBase(server.getBaseUrl());
        AttackHttpClient.HttpResult smart = httpClient.get(base + "/.well-known/smart-configuration");
        AttackHttpClient.HttpResult meta = httpClient.get(base + "/metadata");

        boolean advertised =
                authEndpointSupport.smartConfigIndicatesOAuth(smart.responseBody(), smart.statusCode())
                        || authEndpointSupport.capabilityIndicatesOAuth(meta.responseBody(), meta.statusCode());

        AttackHttpClient.HttpResult patient = httpClient.get(base + "/Patient?_count=1");
        int code = patient.statusCode();
        String detail = "well-known HTTP " + smart.statusCode() + ", metadata HTTP " + meta.statusCode()
                + "; GET /Patient?_count=1 → HTTP " + code + ". "
                + "Sample: " + AuthProbeUtils.truncate(patient.responseBody(), 1200);

        if (!advertised) {
            if (code == 200 || code == 201) {
                return AttackOutcome.openPolicy(
                        code,
                        detail,
                        "No OAuth/SMART in metadata; anonymous Patient read matches typical public demo policy."
                );
            }
            if (code == 401 || code == 403) {
                return AttackOutcome.secure(
                        code,
                        detail,
                        "Patient read rejected without credentials; no OAuth advertised (endpoint may still be protected)."
                );
            }
            if (code == 500) {
                return AttackOutcome.inconclusive(code, detail, "Server error on unauthenticated Patient read.");
            }
            return AttackOutcome.inconclusive(code, detail, "Unexpected status without advertised OAuth: " + code);
        }

        if (code == 200 || code == 201) {
            return AttackOutcome.misconfigured(
                    code,
                    detail,
                    "OAuth/SMART is advertised but unauthenticated Patient read succeeded (policy inconsistency).",
                    AttackSeverity.MEDIUM
            );
        }
        if (code == 401 || code == 403) {
            return AttackOutcome.secure(
                    code,
                    detail,
                    "Unauthenticated read rejected while OAuth/SMART is advertised."
            );
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, detail, "Server error; cannot confirm enforcement vs advertised OAuth.");
        }
        return AttackOutcome.inconclusive(code, detail, "Unexpected status with advertised OAuth: " + code);
    }
}
