package com.fhir.security.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Discovers SMART / OAuth hints from well-known and CapabilityStatement JSON.
 * OAuth is considered advertised only from structured security / SMART config fields,
 * not from narrative keywords anywhere in the CapabilityStatement body.
 */
@Component
public class AuthEndpointSupport {

    private static final String OAUTH_URIS_EXTENSION =
            "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris";

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * True only when SMART well-known returns HTTP 200 and JSON contains a usable {@code token_endpoint} string.
     */
    public boolean smartConfigIndicatesOAuth(String smartBody, int smartStatus) {
        if (smartStatus != 200 || smartBody == null || smartBody.isBlank()) {
            return false;
        }
        return parseTokenEndpointFromSmartJson(smartBody).isPresent();
    }

    /**
     * True when CapabilityStatement declares OAuth/SMART in {@code rest[].security} (extensions or service codings),
     * or exposes a token URL via the standard oauth-uris extension. Does not scan the full document for keywords.
     */
    public boolean capabilityIndicatesOAuth(String metadataBody, int metadataStatus) {
        if (metadataStatus != 200 || metadataBody == null || metadataBody.isBlank()) {
            return false;
        }
        if (parseTokenEndpointFromCapability(metadataBody).isPresent()) {
            return true;
        }
        try {
            JsonNode root = mapper.readTree(metadataBody);
            return restSecurityAdvertisesOAuth(root);
        } catch (Exception ignored) {
            return false;
        }
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
            if (te == null || !te.isTextual()) {
                return Optional.empty();
            }
            String url = te.asText().trim();
            if (url.isBlank() || !looksLikeHttpUrl(url)) {
                return Optional.empty();
            }
            return Optional.of(url);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static boolean looksLikeHttpUrl(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.startsWith("https://") || lower.startsWith("http://");
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
        if (security == null) {
            return Optional.empty();
        }
        if (security.isArray()) {
            for (JsonNode sec : security) {
                Optional<String> fromExt = findOAuthUrisToken(sec.get("extension"));
                if (fromExt.isPresent()) {
                    return fromExt;
                }
            }
        } else if (security.isObject()) {
            return findOAuthUrisToken(security.get("extension"));
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
     * Strict: only {@code rest[].security} extension URLs and service codings (not document-wide text).
     */
    private boolean restSecurityAdvertisesOAuth(JsonNode root) {
        JsonNode rest = root.get("rest");
        if (rest == null || !rest.isArray()) {
            return false;
        }
        for (JsonNode r : rest) {
            JsonNode security = r.get("security");
            if (security == null) {
                continue;
            }
            if (security.isArray()) {
                for (JsonNode sec : security) {
                    if (securityEntryAdvertisesOAuth(sec)) {
                        return true;
                    }
                }
            } else if (security.isObject()) {
                if (securityEntryAdvertisesOAuth(security)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean securityEntryAdvertisesOAuth(JsonNode sec) {
        return extensionsInSecurityIndicateOAuth(sec.get("extension"))
                || serviceIndicatesOAuth(sec.get("service"));
    }

    private boolean extensionsInSecurityIndicateOAuth(JsonNode extensions) {
        if (extensions == null || !extensions.isArray()) {
            return false;
        }
        for (JsonNode ext : extensions) {
            if (extensionUrlMatchesOAuthSignals(text(ext.get("url")))) {
                return true;
            }
            JsonNode inner = ext.get("extension");
            if (inner != null && inner.isArray()) {
                for (JsonNode e : inner) {
                    if (extensionUrlMatchesOAuthSignals(text(e.get("url")))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean extensionUrlMatchesOAuthSignals(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String u = url.toLowerCase(Locale.ROOT);
        return u.contains("smart-capabilities")
                || u.contains("oauth-uris")
                || u.contains("token")
                || u.contains("authorize");
    }

    private boolean serviceIndicatesOAuth(JsonNode service) {
        if (service == null || !service.isArray()) {
            return false;
        }
        for (JsonNode cc : service) {
            JsonNode codings = cc.get("coding");
            if (codings != null && codings.isArray()) {
                for (JsonNode coding : codings) {
                    String code = text(coding.get("code"));
                    String display = text(coding.get("display"));
                    if ("SMART-on-FHIR".equals(code)) {
                        return true;
                    }
                    if (display != null) {
                        String d = display.toLowerCase(Locale.ROOT);
                        if (d.contains("oauth") || d.contains("smart")) {
                            return true;
                        }
                    }
                }
            }
            String text = text(cc.get("text"));
            if (text != null) {
                String t = text.toLowerCase(Locale.ROOT);
                if (t.contains("oauth") || t.contains("smart")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String text(JsonNode n) {
        return n != null && n.isTextual() ? n.asText() : null;
    }
}
