package com.fhir.security.attack;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttackOutcomeAuthenticatedIsolationTest {

    @Test
    void outOfScopeRead_403_is_secure() {
        AttackResult r = AttackOutcome.authenticatedOutOfScopePatientRead(403, "denied", true);
        assertEquals(AttackClassification.SECURE, r.classification());
        assertEquals(AttackSeverity.INFO, r.severity());
    }

    @Test
    void outOfScopeRead_200_with_oauth_is_critical() {
        AttackResult r = AttackOutcome.authenticatedOutOfScopePatientRead(200, "{\"resourceType\":\"Patient\"}", true);
        assertEquals(AttackClassification.VULNERABLE, r.classification());
        assertEquals(AttackSeverity.CRITICAL, r.severity());
    }

    @Test
    void outOfScopeRead_200_without_oauth_is_high() {
        AttackResult r = AttackOutcome.authenticatedOutOfScopePatientRead(200, "{\"resourceType\":\"Patient\"}", false);
        assertEquals(AttackClassification.VULNERABLE, r.classification());
        assertEquals(AttackSeverity.HIGH, r.severity());
    }

    @Test
    void outOfScopeRead_404_is_secure() {
        AttackResult r = AttackOutcome.authenticatedOutOfScopePatientRead(404, "", true);
        assertEquals(AttackClassification.SECURE, r.classification());
    }
}
