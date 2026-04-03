package com.fhir.security.attack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class FhirResourceIdExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FhirResourceIdExtractor() {
    }

    public static String extractId(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }

        try {
            JsonNode root = MAPPER.readTree(responseBody);
            if (root == null) {
                return null;
            }

            if (root.has("id") && root.get("id") != null && !root.get("id").isNull()) {
                return normalizeId(root.get("id").asText());
            }

            // Some servers may wrap created resources in Bundle.entry[].resource
            if (root.has("resourceType") && "Bundle".equalsIgnoreCase(root.get("resourceType").asText())
                    && root.has("entry") && root.get("entry").isArray()) {
                for (JsonNode entry : root.get("entry")) {
                    if (entry == null) continue;
                    JsonNode resNode = entry.get("resource");
                    if (resNode != null && resNode.has("id")) {
                        String id = resNode.get("id").asText();
                        if (id != null && !id.isBlank()) {
                            return normalizeId(id);
                        }
                    }
                }
            }

            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String normalizeId(String rawId) {
        if (rawId == null) return null;
        String id = rawId.trim();
        if (id.isEmpty()) return null;
        if (id.contains("/")) {
            return id.substring(id.lastIndexOf('/') + 1);
        }
        return id;
    }
}

