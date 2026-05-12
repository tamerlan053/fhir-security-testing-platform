package com.fhir.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Optional lab credentials for Week 11 authenticated isolation probes.
 * Set via environment variables — never commit real tokens.
 */
@ConfigurationProperties(prefix = "fhir.security.test")
public class FhirSecurityTestProperties {

    /**
     * OAuth bearer token for isolation tests (without the "Bearer " prefix).
     */
    private String bearerToken = "";

    /**
     * When non-blank, the token probe runs only if the server base URL contains this substring (case-sensitive).
     */
    private String labBaseUrlContains = "";

    /**
     * Logical id of a Patient that must not be readable with the test token (outside granted context).
     */
    private String outOfScopePatientId = "";

    public String getBearerToken() {
        return bearerToken != null ? bearerToken : "";
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public String getLabBaseUrlContains() {
        return labBaseUrlContains != null ? labBaseUrlContains : "";
    }

    public void setLabBaseUrlContains(String labBaseUrlContains) {
        this.labBaseUrlContains = labBaseUrlContains;
    }

    public String getOutOfScopePatientId() {
        return outOfScopePatientId != null ? outOfScopePatientId : "";
    }

    public void setOutOfScopePatientId(String outOfScopePatientId) {
        this.outOfScopePatientId = outOfScopePatientId;
    }
}
