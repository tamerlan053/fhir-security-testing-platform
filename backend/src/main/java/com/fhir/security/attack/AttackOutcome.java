package com.fhir.security.attack;

/**
 * Factory methods for consistent classification across attacks.
 */
public final class AttackOutcome {

    private AttackOutcome() {}

    public static AttackResult secure(int statusCode, String responseBody, String reason) {
        return AttackResult.of(
                statusCode,
                responseBody,
                AttackClassification.SECURE,
                reason,
                AttackSeverity.INFO
        );
    }

    public static AttackResult vulnerable(int statusCode, String responseBody, String reason, AttackSeverity severity) {
        return AttackResult.of(
                statusCode,
                responseBody,
                AttackClassification.VULNERABLE,
                reason,
                severity
        );
    }

    public static AttackResult openPolicy(int statusCode, String responseBody, String reason) {
        return AttackResult.of(
                statusCode,
                responseBody,
                AttackClassification.OPEN_POLICY,
                reason,
                AttackSeverity.INFO
        );
    }

    public static AttackResult inconclusive(int statusCode, String responseBody, String reason) {
        return AttackResult.of(
                statusCode,
                responseBody,
                AttackClassification.INCONCLUSIVE,
                reason,
                AttackSeverity.LOW
        );
    }

    /**
     * Invalid / malformed FHIR body on POST (validation attacks).
     */
    public static AttackResult validationPost(int statusCode, String responseBody) {
        if (statusCode == 400 || statusCode == 404 || statusCode == 405 || statusCode == 422 || statusCode == 412) {
            return secure(
                    statusCode,
                    responseBody,
                    "Server rejected the non-conformant or invalid request as expected."
            );
        }
        if (statusCode == 200 || statusCode == 201) {
            return vulnerable(
                    statusCode,
                    responseBody,
                    "Server accepted a payload that should fail validation.",
                    AttackSeverity.HIGH
            );
        }
        if (statusCode == 401 || statusCode == 403) {
            return secure(
                    statusCode,
                    responseBody,
                    "Request rejected; endpoint may require authentication for writes."
            );
        }
        if (statusCode == 500) {
            return inconclusive(
                    statusCode,
                    responseBody,
                    "Server error; cannot determine whether validation is correct."
            );
        }
        return inconclusive(
                statusCode,
                responseBody,
                "Unexpected HTTP status for validation probe: " + statusCode
        );
    }

    /**
     * GET with invalid Basic / forged Bearer on a read endpoint.
     */
    public static AttackResult authReadWithBadCredentials(int statusCode, String responseBody, boolean oauthAdvertised) {
        if (statusCode == 401 || statusCode == 403) {
            return secure(
                    statusCode,
                    responseBody,
                    "Server rejected the request with invalid or forged credentials."
            );
        }
        if (statusCode == 200 || statusCode == 201) {
            if (oauthAdvertised) {
                return vulnerable(
                        statusCode,
                        responseBody,
                        "Read succeeded with invalid/forged credentials while OAuth/SMART is advertised in metadata.",
                        AttackSeverity.HIGH
                );
            }
            return openPolicy(
                    statusCode,
                    responseBody,
                    "Read succeeded with bad credentials ignored — typical for public demo servers without advertised OAuth."
            );
        }
        if (statusCode == 500) {
            return inconclusive(
                    statusCode,
                    responseBody,
                    "Server error on auth read probe (may hide proper 401/403 handling)."
            );
        }
        return inconclusive(statusCode, responseBody, "Unexpected status on auth read probe: " + statusCode);
    }

    /**
     * Unauthenticated write (POST/PUT) — anonymous success is serious if auth is advertised.
     */
    public static AttackResult anonymousWrite(int statusCode, String responseBody, boolean oauthAdvertised) {
        if (statusCode == 401 || statusCode == 403) {
            return secure(
                    statusCode,
                    responseBody,
                    "Write rejected without proper authorization."
            );
        }
        if (statusCode == 200 || statusCode == 201) {
            if (oauthAdvertised) {
                return vulnerable(
                        statusCode,
                        responseBody,
                        "Write succeeded without valid credentials while OAuth/SMART is advertised.",
                        AttackSeverity.CRITICAL
                );
            }
            return openPolicy(
                    statusCode,
                    responseBody,
                    "Anonymous write succeeded — common on open sandboxes for testing (no OAuth advertised)."
            );
        }
        if (statusCode == 500) {
            return inconclusive(
                    statusCode,
                    responseBody,
                    "Server error during write probe."
            );
        }
        if (statusCode == 400 || statusCode == 404 || statusCode == 405 || statusCode == 412) {
            return secure(
                    statusCode,
                    responseBody,
                    "Write rejected or not allowed for this request."
            );
        }
        return inconclusive(statusCode, responseBody, "Unexpected status on write probe: " + statusCode);
    }

    /**
     * GET access to another patient&apos;s resource (IDOR-style read).
     */
    /**
     * Read of a Patient using a valid lab token where the id must be outside the granted context (Week 11).
     */
    public static AttackResult authenticatedOutOfScopePatientRead(int statusCode, String responseBody, boolean oauthAdvertised) {
        if (statusCode == 401 || statusCode == 403) {
            return secure(
                    statusCode,
                    responseBody,
                    "Out-of-scope Patient read rejected with the configured bearer token (isolation looks enforced)."
            );
        }
        if (statusCode == 200 || statusCode == 201) {
            if (oauthAdvertised) {
                return vulnerable(
                        statusCode,
                        responseBody,
                        "Bearer token could read a Patient id configured as outside the granted context while OAuth/SMART is advertised (broken isolation / escalation risk).",
                        AttackSeverity.CRITICAL
                );
            }
            return vulnerable(
                    statusCode,
                    responseBody,
                    "Bearer token could read a Patient id configured as outside the granted context (broken isolation on this server).",
                    AttackSeverity.HIGH
            );
        }
        if (statusCode == 404) {
            return secure(
                    statusCode,
                    responseBody,
                    "Patient not found or not visible with the test token (no cross-context read signal)."
            );
        }
        if (statusCode == 500) {
            return inconclusive(
                    statusCode,
                    responseBody,
                    "Server error during authenticated out-of-scope read probe."
            );
        }
        return inconclusive(statusCode, responseBody, "Unexpected HTTP status on authenticated out-of-scope read: " + statusCode);
    }

    public static AttackResult crossPatientRead(int statusCode, String responseBody, boolean oauthAdvertised) {
        if (statusCode == 401 || statusCode == 403) {
            return secure(
                    statusCode,
                    responseBody,
                    "Cross-patient read rejected."
            );
        }
        if (statusCode == 200 || statusCode == 201) {
            if (oauthAdvertised) {
                return vulnerable(
                        statusCode,
                        responseBody,
                        "Read of another patient resource succeeded while OAuth/SMART is advertised (possible broken object-level authorization).",
                        AttackSeverity.HIGH
                );
            }
            return openPolicy(
                    statusCode,
                    responseBody,
                    "Cross-patient read succeeded — typical for fully open public FHIR demos (no OAuth in metadata)."
            );
        }
        if (statusCode == 500) {
            return inconclusive(
                    statusCode,
                    responseBody,
                    "Server error on cross-patient read probe."
            );
        }
        if (statusCode == 404) {
            return secure(
                    statusCode,
                    responseBody,
                    "Resource not found or not exposed (read denied by absence)."
            );
        }
        return inconclusive(statusCode, responseBody, "Unexpected status on cross-patient read: " + statusCode);
    }

    public static AttackResult setupCreateFailed(int statusCode, String responseBody) {
        if (statusCode == 401 || statusCode == 403) {
            return inconclusive(
                    statusCode,
                    responseBody,
                    "Cannot complete scenario: Patient creation requires authentication."
            );
        }
        return inconclusive(
                statusCode,
                responseBody,
                "Cannot complete scenario: Patient creation failed with HTTP " + statusCode + "."
        );
    }

    /**
     * Fold {@link #combineWorst} over several probe results (same body accumulation order as sequential combines).
     */
    public static AttackResult combineWorstAll(AttackResult first, AttackResult... rest) {
        AttackResult acc = first;
        StringBuilder body = new StringBuilder(first.responseBody() != null ? first.responseBody() : "");
        for (AttackResult r : rest) {
            body.append("\n---\n").append(r.responseBody() != null ? r.responseBody() : "");
            acc = combineWorst(acc, r, body.toString());
        }
        return acc;
    }

    /**
     * Pick the worst classification when combining multiple probes (highest risk first).
     */
    public static AttackResult combineWorst(AttackResult a, AttackResult b, String combinedBody) {
        AttackResult primary = priority(a.classification()) >= priority(b.classification()) ? a : b;
        AttackResult secondary = primary == a ? b : a;
        String mergedReason = primary.reason()
                + " | Also: " + secondary.classification() + " — " + secondary.reason();
        int code = primary.statusCode() != 0 ? primary.statusCode() : secondary.statusCode();
        LeakageExposure mergedLeak = ResponseBodyLeakageAnalyzer.worst(
                ResponseBodyLeakageAnalyzer.worst(primary.leakageExposure(), secondary.leakageExposure()),
                ResponseBodyLeakageAnalyzer.analyze(code, combinedBody)
        );
        return AttackResult.of(
                code,
                combinedBody,
                primary.classification(),
                mergedReason,
                worstSeverity(primary.severity(), secondary.severity()),
                AttackVectorCatalog.mergeIds(primary.attackVectorIds(), secondary.attackVectorIds()),
                mergedLeak
        );
    }

    private static int priority(AttackClassification c) {
        return switch (c) {
            case VULNERABLE -> 4;
            case INCONCLUSIVE -> 3;
            case OPEN_POLICY -> 2;
            case SECURE -> 1;
        };
    }

    private static AttackSeverity worstSeverity(AttackSeverity a, AttackSeverity b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}
