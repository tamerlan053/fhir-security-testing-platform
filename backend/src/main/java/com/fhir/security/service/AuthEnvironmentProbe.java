package com.fhir.security.service;

import org.springframework.stereotype.Component;

/**
 * Detects whether the FHIR server advertises OAuth/SMART (metadata / well-known) for classification.
 */
@Component
public class AuthEnvironmentProbe {

    private final AttackHttpClient httpClient;
    private final AuthEndpointSupport authEndpointSupport;

    public AuthEnvironmentProbe(AttackHttpClient httpClient, AuthEndpointSupport authEndpointSupport) {
        this.httpClient = httpClient;
        this.authEndpointSupport = authEndpointSupport;
    }

    /**
     * {@code true} if SMART well-known or CapabilityStatement suggests OAuth usage.
     */
    public boolean isOAuthAdvertised(String baseUrl) {
        String base = baseUrl.replaceAll("/$", "");
        AttackHttpClient.HttpResult smart = httpClient.get(base + "/.well-known/smart-configuration");
        AttackHttpClient.HttpResult meta = httpClient.get(base + "/metadata");
        return authEndpointSupport.smartConfigIndicatesOAuth(smart.responseBody(), smart.statusCode())
                || authEndpointSupport.capabilityIndicatesOAuth(meta.responseBody(), meta.statusCode());
    }
}
