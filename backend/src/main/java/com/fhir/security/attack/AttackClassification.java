package com.fhir.security.attack;

/**
 * Result of a security probe beyond a binary vulnerable flag.
 * Only {@link #VULNERABLE} increments legacy &quot;vulnerability&quot; counts.
 */
public enum AttackClassification {
    /** Expected safe rejection or healthy behavior. */
    SECURE,
    /** Confirmed security weakness (counts as vulnerability). */
    VULNERABLE,
    /** Matches intentional public/demo policy (e.g. anonymous read on sandbox). */
    OPEN_POLICY,
    /** Advertised security (e.g. OAuth) inconsistent with observed access. */
    MISCONFIGURED,
    /** Cannot determine (errors, incomplete setup, or not applicable). */
    INCONCLUSIVE
}
