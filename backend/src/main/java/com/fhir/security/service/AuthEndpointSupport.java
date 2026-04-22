package com.fhir.security.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Discovers SMART / OAuth hints from well-known and CapabilityStatement JSON.
 */
@Component
public class AuthEndpointSupport {

    private static final String OAUTH_URIS_EXTENSION =
            "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris";

    private final ObjectMapper mapper = new ObjectMapper();

    public boolean smartConfigIndicatesOAuth(String smartBody, int smartStatus) {
        if (smartStatus != 200 || smartBody == null || smartBody.isBlank()) {
            return false;
        }
        return parseTokenEndpointFromSmartJson(smartBody).isPresent();
    }

    public boolean capabilityIndicatesOAuth(String metadataBody, int metadataStatus) {
        if (metadataStatus != 200 || metadataBody == null || metadataBody.isBlank()) {
            return false;
        }
        if (parseTokenEndpointFromCapability(metadataBody).isPresent()) {
            return true;
        }
        return securityServiceContainsOAuthHeuristic(metadataBody);
    }

    public Optional<String> findTokenEndpoint(AttackHttpClient.HttpResult smart, AttackHttpClient.HttpResult metadata) {
        if (smart.statusCode() == 200) {
            Optional<String> fromSmart = parseTokenEndpointFromSmartJson(smart.responseBody());
            if (fromSmart.isPresent()) {
                return fromSmart;
            }
        }
        if (metadata.statusCode() == 200) {
            return parseTokenEndpointFromCapability(metadata.responseBody());
        }
        return Optional.empty();
    }

    private Optional<String> parseTokenEndpointFromSmartJson(String body) {
        try {
            JsonNode root = mapper.readTree(body);
            JsonNode te = root.get("token_endpoint");
            if (te != null && te.isTextual() && !te.asText().isBlank()) {
                return Optional.of(te.asText());
            }
        } catch (Exception ignored) {
            // fall through
        }
        return Optional.empty();
    }

    private Optional<String> parseTokenEndpointFromCapability(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode rest = root.get("rest");
            if (rest == null || !rest.isArray()) {
                return Optional.empty();
            }
            for (JsonNode r : rest) {
                Optional<String> t = extractTokenFromSecurity(r.get("security"));
                if (t.isPresent()) {
                    return t;
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return Optional.empty();
    }

    private Optional<String> extractTokenFromSecurity(JsonNode security) {
        if (security == null || !security.isArray()) {
            return Optional.empty();
        }
        for (JsonNode sec : security) {
            Optional<String> fromExt = findOAuthUrisToken(sec.get("extension"));
            if (fromExt.isPresent()) {
                return fromExt;
            }
        }
        return Optional.empty();
    }

    private Optional<String> findOAuthUrisToken(JsonNode extensions) {
        if (extensions == null || !extensions.isArray()) {
            return Optional.empty();
        }
        for (JsonNode ext : extensions) {
            if (!OAUTH_URIS_EXTENSION.equals(text(ext.get("url")))) {
                continue;
            }
            JsonNode inner = ext.get("extension");
            if (inner == null || !inner.isArray()) {
                continue;
            }
            for (JsonNode e : inner) {
                if (!"token".equals(text(e.get("url")))) {
                    continue;
                }
                JsonNode v = e.get("valueUri");
                if (v != null && v.isTextual() && !v.asText().isBlank()) {
                    return Optional.of(v.asText());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Fallback when extensions are non-standard but metadata still mentions OAuth URIs.
     */
    private boolean securityServiceContainsOAuthHeuristic(String json) {
        String lower = json.toLowerCase();
        if (!lower.contains("oauth") && !lower.contains("smart")) {
            return false;
        }
        return lower.contains("token") || lower.contains("authorize") || lower.contains("oauth");
    }

    private static String text(JsonNode n) {
        return n != null && n.isTextual() ? n.asText() : null;
    }
}
