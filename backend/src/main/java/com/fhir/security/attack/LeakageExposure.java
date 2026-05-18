package com.fhir.security.attack;

/**
 * Heuristic classification of whether HTTP response bodies may expose implementation
 * or infrastructure details beyond a minimal client-facing error (Week 10).
 */     
public enum LeakageExposure {
    /** Short or generic response; no suspicious patterns detected. */
    NONE,
    /** Large diagnostic text, nested errors, or unusually verbose OperationOutcome. */
    VERBOSE_ERROR_BODY,
    /** Patterns suggesting stack traces, framework internals, paths, or driver messages. */
    IMPLEMENTATION_DETAIL_LEAK
}
