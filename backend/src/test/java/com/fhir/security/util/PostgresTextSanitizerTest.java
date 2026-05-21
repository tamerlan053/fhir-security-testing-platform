package com.fhir.security.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PostgresTextSanitizerTest {

    @Test
    void replacesNulBytesWithVisibleEscape() {
        assertEquals("visible\\u0000hidden", PostgresTextSanitizer.sanitize("visible\u0000hidden"));
    }

    @Test
    void leavesCleanTextUnchanged() {
        assertEquals("ok", PostgresTextSanitizer.sanitize("ok"));
    }

    @Test
    void nullInputReturnsNull() {
        assertNull(PostgresTextSanitizer.sanitize(null));
    }
}
