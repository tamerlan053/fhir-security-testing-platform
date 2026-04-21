package com.fhir.security.attack;

import com.fhir.security.entity.FhirServer;
import com.fhir.security.service.AttackHttpClient;
import com.fhir.security.service.AuthEndpointSupport;
import com.fhir.security.service.AuthEnvironmentProbe;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Consolidated authentication probes: Basic, Bearer (forged / malformed), and OAuth token endpoint.
 */
@Component
@Order(70)
public class InvalidCredentialsAccessAttack implements ExecutableAttack {

    private static final String INVALID_BASIC = "Basic aW52YWxpZDppbnZhbGlk";
    private static final String SYNTHETIC_JWT =
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
                    + "eyJzdWIiOiJ3ZWVrN19mYWtlIiwiZXhwIjo5OTk5OTk5OTk5fQ."
                    + "invalid_signature_week7";
    private static final String EXPIRED_SHAPED =
            "Bearer eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0."
                    + "eyJleHAiOjE1MDAwMDAwMDAsInN1YiI6IndlZWs3In0."
                    + "x";

    private final AttackHttpClient httpClient;
    private final AuthEnvironmentProbe authEnvironmentProbe;
    private final AuthEndpointSupport authEndpointSupport;

    public InvalidCredentialsAccessAttack(
            AttackHttpClient httpClient,
            AuthEnvironmentProbe authEnvironmentProbe,
            AuthEndpointSupport authEndpointSupport
    ) {
        this.httpClient = httpClient;
        this.authEnvironmentProbe = authEnvironmentProbe;
        this.authEndpointSupport = authEndpointSupport;
    }

    @Override
    public String getName() {
        return "Invalid Credentials Access Test";
    }

    @Override
    public String getDescription() {
        return "Invalid Basic, forged/malformed Bearer on reads, and invalid OAuth token grant where discoverable";
    }

    @Override
    public AttackResult execute(FhirServer server) {
        String base = AuthProbeUtils.normalizeBase(server.getBaseUrl());
        boolean oauthAdvertised = authEnvironmentProbe.isOAuthAdvertised(base);

        AttackResult oauthProbe = runOAuthTokenProbe(base);
        AttackResult basicProbe = runBasicProbe(base, oauthAdvertised);
        AttackResult bearerProbe = runSyntheticBearerProbes(base, oauthAdvertised);
        AttackResult malformedBearerProbe = runMalformedBearerProbes(base, oauthAdvertised);

        return AttackOutcome.combineWorstAll(oauthProbe, basicProbe, bearerProbe, malformedBearerProbe);
    }

    private AttackResult runOAuthTokenProbe(String base) {
        AttackHttpClient.HttpResult smart = httpClient.get(base + "/.well-known/smart-configuration");
        AttackHttpClient.HttpResult meta = httpClient.get(base + "/metadata");
        Optional<String> tokenUrl = authEndpointSupport.findTokenEndpoint(smart, meta);
        if (tokenUrl.isEmpty()) {
            String note = "No token_endpoint discovered.";
            return AttackOutcome.inconclusive(meta.statusCode(), note, "OAuth token URL not available; sub-probe skipped.");
        }
        String form = "grant_type=client_credentials&client_id=invalid_week7_probe_client";
        AttackHttpClient.HttpResult tokenResp = httpClient.postUrlEncoded(tokenUrl.get(), form, null);
        int code = tokenResp.statusCode();
        String bodyLower = tokenResp.responseBody().toLowerCase();
        boolean issuedTokens = bodyLower.contains("access_token");
        String detail = "token_endpoint → HTTP " + code + ". " + AuthProbeUtils.truncate(tokenResp.responseBody(), 1500);

        if ((code == 200 || code == 201) && issuedTokens) {
            return AttackOutcome.vulnerable(
                    code,
                    detail,
                    "Token endpoint issued tokens for invalid client_credentials.",
                    AttackSeverity.CRITICAL
            );
        }
        if (code == 500) {
            return AttackOutcome.inconclusive(code, detail, "Token endpoint returned 500.");
        }
        if (code == 400 || code == 401 || code == 403) {
            return AttackOutcome.secure(code, detail, "Token endpoint rejected invalid grant.");
        }
        if (code == 200 || code == 201) {
            return AttackOutcome.inconclusive(code, detail, "Token endpoint 200 without access_token; non-standard.");
        }
        return AttackOutcome.inconclusive(code, detail, "Unexpected token endpoint status: " + code);
    }

    private AttackResult runBasicProbe(String base, boolean oauthAdvertised) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, INVALID_BASIC);
        AttackHttpClient.HttpResult r = httpClient.get(base + "/Patient?_count=1", headers);
        return AttackOutcome.authReadWithBadCredentials(
                r.statusCode(),
                AuthProbeUtils.truncate(r.responseBody(), 1500),
                oauthAdvertised
        );
    }

    private AttackResult runSyntheticBearerProbes(String base, boolean oauthAdvertised) {
        HttpHeaders h1 = new HttpHeaders();
        h1.add(HttpHeaders.AUTHORIZATION, SYNTHETIC_JWT);
        AttackHttpClient.HttpResult r1 = httpClient.get(base + "/Patient?_count=1", h1);
        AttackResult a1 = AttackOutcome.authReadWithBadCredentials(
                r1.statusCode(),
                AuthProbeUtils.truncate(r1.responseBody(), 1200),
                oauthAdvertised
        );

        HttpHeaders h2 = new HttpHeaders();
        h2.add(HttpHeaders.AUTHORIZATION, SYNTHETIC_JWT);
        AttackHttpClient.HttpResult r2 = httpClient.get(base + "/Observation?_count=1", h2);
        AttackResult a2 = AttackOutcome.authReadWithBadCredentials(
                r2.statusCode(),
                AuthProbeUtils.truncate(r2.responseBody(), 1200),
                oauthAdvertised
        );

        return AttackOutcome.combineWorst(a1, a2, "Bearer probes Patient " + r1.statusCode() + " | Observation " + r2.statusCode());
    }

    private AttackResult runMalformedBearerProbes(String base, boolean oauthAdvertised) {
        String url = base + "/Patient?_count=1";
        String[] values = {
                "Bearer ",
                "Bearer not.a.valid.jwt.week7",
                EXPIRED_SHAPED
        };

        AttackResult aggregate = null;
        StringBuilder log = new StringBuilder();
        for (String auth : values) {
            HttpHeaders h = new HttpHeaders();
            h.add(HttpHeaders.AUTHORIZATION, auth);
            AttackHttpClient.HttpResult r = httpClient.get(url, h);
            log.append("Bearer variant → HTTP ").append(r.statusCode()).append("; ");
            AttackResult step = AttackOutcome.authReadWithBadCredentials(
                    r.statusCode(),
                    AuthProbeUtils.truncate(r.responseBody(), 400),
                    oauthAdvertised
            );
            if (aggregate == null) {
                aggregate = step;
            } else {
                aggregate = AttackOutcome.combineWorst(aggregate, step, log.toString());
            }
        }
        return new AttackResult(
                aggregate.statusCode(),
                log.toString(),
                aggregate.classification(),
                aggregate.reason(),
                aggregate.severity()
        );
    }
}
