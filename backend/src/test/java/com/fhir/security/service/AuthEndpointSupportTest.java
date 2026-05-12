package com.fhir.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthEndpointSupportTest {

    private AuthEndpointSupport support;

    @BeforeEach
    void setUp() {
        support = new AuthEndpointSupport();
    }

    @Test
    void smartConfig_false_when_status_not_200() {
        assertFalse(support.smartConfigIndicatesOAuth("{\"token_endpoint\":\"https://a/t\"}", 404));
    }

    @Test
    void smartConfig_false_when_no_token_endpoint_key() {
        assertFalse(support.smartConfigIndicatesOAuth(
                "{\"issuer\":\"https://x\",\"note\":\"token_endpoint is not here\"}", 200));
    }

    @Test
    void smartConfig_false_when_token_endpoint_not_http_url() {
        assertFalse(support.smartConfigIndicatesOAuth("{\"token_endpoint\":\"not-a-valid-url\"}", 200));
    }

    @Test
    void smartConfig_true_when_token_endpoint_is_https() {
        assertTrue(support.smartConfigIndicatesOAuth(
                "{\"token_endpoint\":\"https://auth.example.org/oauth2/token\"}", 200));
    }

    @Test
    void capability_false_when_keywords_only_in_narrative_empty_security() {
        String json = """
                {
                  "resourceType": "CapabilityStatement",
                  "description": "This server supports OAuth2 SMART on FHIR token authorize flows",
                  "rest": [{"mode": "server", "security": []}]
                }
                """;
        assertFalse(support.capabilityIndicatesOAuth(json, 200));
    }

    @Test
    void capability_false_when_security_empty_object_like_public_servers() {
        String json = """
                {
                  "resourceType": "CapabilityStatement",
                  "rest": [{"mode": "server", "security": [{}]}]
                }
                """;
        assertFalse(support.capabilityIndicatesOAuth(json, 200));
    }

    @Test
    void capability_false_when_security_only_cors_extension() {
        String json = """
                {
                  "resourceType": "CapabilityStatement",
                  "rest": [{
                    "mode": "server",
                    "security": [{
                      "extension": [{"url": "http://fhir-registry.smarthealthit.org/StructureDefinition/cors", "valueBoolean": true}]
                    }]
                  }]
                }
                """;
        assertFalse(support.capabilityIndicatesOAuth(json, 200));
    }

    @Test
    void capability_true_when_oauth_uris_extension_has_token() {
        String json = """
                {
                  "resourceType": "CapabilityStatement",
                  "rest": [{
                    "security": [{
                      "extension": [{
                        "url": "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris",
                        "extension": [
                          {"url": "token", "valueUri": "https://auth.example/token"},
                          {"url": "authorize", "valueUri": "https://auth.example/authorize"}
                        ]
                      }]
                    }]
                  }]
                }
                """;
        assertTrue(support.capabilityIndicatesOAuth(json, 200));
    }

    @Test
    void capability_true_when_security_extension_url_signals_smart_capabilities() {
        String json = """
                {
                  "resourceType": "CapabilityStatement",
                  "rest": [{
                    "security": [{
                      "extension": [{"url": "http://example.org/fhir/smart-capabilities"}]
                    }]
                  }]
                }
                """;
        assertTrue(support.capabilityIndicatesOAuth(json, 200));
    }

    @Test
    void capability_true_when_service_coding_is_smart_on_fhir() {
        String json = """
                {
                  "resourceType": "CapabilityStatement",
                  "rest": [{
                    "security": [{
                      "service": [{
                        "coding": [{
                          "system": "http://terminology.hl7.org/CodeSystem/restful-security-service",
                          "code": "SMART-on-FHIR"
                        }]
                      }]
                    }]
                  }]
                }
                """;
        assertTrue(support.capabilityIndicatesOAuth(json, 200));
    }

    @Test
    void capability_true_when_service_display_mentions_oauth() {
        String json = """
                {
                  "resourceType": "CapabilityStatement",
                  "rest": [{
                    "security": [{
                      "service": [{
                        "coding": [{"display": "OAuth 2.0"}]
                      }]
                    }]
                  }]
                }
                """;
        assertTrue(support.capabilityIndicatesOAuth(json, 200));
    }
}
