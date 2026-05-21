package com.fhir.security.util;

/**
 * PostgreSQL {@code TEXT} columns reject NUL ({@code 0x00}) bytes in UTF-8 strings.
 */
public final class PostgresTextSanitizer {

    private PostgresTextSanitizer() {}

    public static String sanitize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (text.indexOf('\u0000') < 0) {
            return text;
        }
        return text.replace("\u0000", "\\u0000");
    }
}
