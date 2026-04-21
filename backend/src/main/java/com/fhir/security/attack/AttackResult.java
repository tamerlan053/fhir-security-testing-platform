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
        AttackSeverity severity
) {
    /**
     * @deprecated use {@link #classification()} == {@link AttackClassification#VULNERABLE}
     */
    public boolean vulnerable() {
        return classification == AttackClassification.VULNERABLE;
    }
}
