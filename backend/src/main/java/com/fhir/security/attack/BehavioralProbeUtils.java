package com.fhir.security.attack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Small helpers for "deep" behavioral checks without changing public APIs.
 * Intentionally string/JSON-lightweight: avoids adding new DTOs or dependencies.
 */
public final class BehavioralProbeUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private BehavioralProbeUtils() {}

    public static boolean isOperationOutcome(String body) {
        if (body == null || body.isBlank()) return false;
        try {
            JsonNode root = MAPPER.readTree(body);
            return root != null
                    && root.has("resourceType")
                    && "OperationOutcome".equalsIgnoreCase(root.get("resourceType").asText());
        } catch (Exception ignored) {
            return false;
        }
    }

    public static String safeExtractString(String body, String fieldName) {
        if (body == null || body.isBlank()) return null;
        try {
            JsonNode root = MAPPER.readTree(body);
            if (root != null && root.has(fieldName) && root.get(fieldName) != null && root.get(fieldName).isTextual()) {
                String v = root.get(fieldName).asText();
                return v != null && !v.isBlank() ? v : null;
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }

    public static boolean containsIgnoreCase(String haystack, String needle) {
        if (haystack == null || needle == null) return false;
        return haystack.toLowerCase().contains(needle.toLowerCase());
    }

    /**
     * Returns true when the response signals a duplicate-resource rejection (e.g. HAPI-2840).
     * These errors must be treated as INCONCLUSIVE, not SECURE, because the server rejected the
     * request before it could evaluate the malicious payload.
     */
    public static boolean isDuplicateResourceError(String body) {
        if (body == null || body.isBlank()) return false;
        String lower = body.toLowerCase();
        return lower.contains("hapi-2840")
                || lower.contains("can not create resource duplicating existing resource")
                || lower.contains("duplicate");
    }
}
