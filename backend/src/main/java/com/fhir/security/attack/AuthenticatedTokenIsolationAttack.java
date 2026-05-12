package com.fhir.security.attack;

import com.fhir.security.config.FhirSecurityTestProperties;
import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import com.fhir.security.service.AuthEnvironmentProbe;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * When {@code fhir.security.test.*} / env vars supply a bearer token and out-of-scope patient id,
 * probes whether that token can read another patient's resource (Week 11).
 */
@Component
@Order(84)
public class AuthenticatedTokenIsolationAttack implements ExecutableAttack {

    private final AttackHttpClient httpClient;
    private final AuthEnvironmentProbe authEnvironmentProbe;
    private final FhirSecurityTestProperties testProperties;

    public AuthenticatedTokenIsolationAttack(
            AttackHttpClient httpClient,
            AuthEnvironmentProbe authEnvironmentProbe,
            FhirSecurityTestProperties testProperties
    ) {
        this.httpClient = httpClient;
        this.authEnvironmentProbe = authEnvironmentProbe;
        this.testProperties = testProperties;
    }

    @Override
    public String getName() {
        return "Authenticated Token Isolation";
    }

    @Override
    public String getDescription() {
        return "Optional: with a configured bearer token, GET Patient/{out-of-scope id} to test isolation outside the granted context";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String base = AuthProbeUtils.normalizeBase(server.getBaseUrl());
        boolean oauthAdvertised = authEnvironmentProbe.isOAuthAdvertised(base);

        String token = testProperties.getBearerToken().trim();
        if (token.isEmpty()) {
            return AttackOutcome.inconclusive(
                    0,
                    "",
                    "N/A: No bearer token configured (set FHIR_SECURITY_TEST_BEARER_TOKEN or fhir.security.test.bearer-token); authenticated isolation probe skipped."
            );
        }

        String patientId = testProperties.getOutOfScopePatientId().trim();
        if (patientId.isEmpty()) {
            return AttackOutcome.inconclusive(
                    0,
                    "",
                    "N/A: No out-of-scope patient id configured (set FHIR_SECURITY_TEST_OUT_OF_SCOPE_PATIENT_ID or fhir.security.test.out-of-scope-patient-id)."
            );
        }

        String filter = testProperties.getLabBaseUrlContains().trim();
        if (!filter.isEmpty() && !server.getBaseUrl().contains(filter)) {
            return AttackOutcome.inconclusive(
                    0,
                    "",
                    "N/A: Server base URL does not match FHIR_SECURITY_TEST_LAB_BASE_CONTAINS / fhir.security.test.lab-base-url-contains filter; probe not run for this server."
            );
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        String url = base + "/Patient/" + patientId;
        AttackHttpClient.HttpResult result = httpClient.get(url, headers);
        String detail = "GET " + url + " with Bearer token → HTTP " + result.statusCode() + ". "
                + AuthProbeUtils.truncate(result.responseBody(), 1500);
        return AttackOutcome.authenticatedOutOfScopePatientRead(
                result.statusCode(),
                detail,
                oauthAdvertised
        );
    }
}
