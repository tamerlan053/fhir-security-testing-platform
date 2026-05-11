package com.fhir.security.attack;

/**
 * Outcome of an {@link ExecutableAttack}.
 * {@link #vulnerable()} is {@code true} only for {@link AttackClassification#VULNERABLE} (backward compatible for API counts).
 */
public record AttackResult(
        int statusCode,
        String responseBody,
        AttackClassification classification,
        String reason,
        AttackSeverity severity,
        String attackVectorIds,
        LeakageExposure leakageExposure
) {
    public AttackResult {
        if (attackVectorIds == null) {
            attackVectorIds = "";
        }
        if (leakageExposure == null) {
            leakageExposure = LeakageExposure.NONE;
        }
    }

    /** Backward-compatible factory: no vector tags, no elevated leakage signal from the attack itself. */
    public static AttackResult of(int statusCode,
                                  String responseBody,
                                  AttackClassification classification,
                                  String reason,
                                  AttackSeverity severity) {
        return new AttackResult(statusCode, responseBody, classification, reason, severity, "", LeakageExposure.NONE);
    }

    public static AttackResult of(int statusCode,
                                  String responseBody,
                                  AttackClassification classification,
                                  String reason,
                                  AttackSeverity severity,
                                  String attackVectorIds,
                                  LeakageExposure leakageExposure) {
        return new AttackResult(statusCode, responseBody, classification, reason, severity, attackVectorIds, leakageExposure);
    }

    /**
     * @deprecated use {@link #classification()} == {@link AttackClassification#VULNERABLE}
     */
    public boolean vulnerable() {
        return classification == AttackClassification.VULNERABLE;
    }
}
