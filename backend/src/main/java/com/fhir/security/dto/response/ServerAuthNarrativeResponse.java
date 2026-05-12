package com.fhir.security.dto.response;

import java.time.LocalDateTime;

/**
 * Consolidated auth / isolation narrative for Week 11 (live metadata signals + latest stored run excerpts).
 */
public record ServerAuthNarrativeResponse(
        long serverId,
        String serverName,
        String baseUrl,
        boolean oauthSmartAdvertised,
        int anonymousPatientReadHttpStatus,
        String authEnvironmentLabel,
        LocalDateTime lastTestRunStartedAt,
        String lastRunCrossPatientClassification,
        String lastRunCrossPatientReason,
        String lastRunOpenEndpointClassification,
        String lastRunOpenEndpointReason,
        String lastRunTokenIsolationClassification,
        String lastRunTokenIsolationReason,
        String lastRunObservationBundleClassification,
        String lastRunObservationBundleReason,
        String narrative
) {}
