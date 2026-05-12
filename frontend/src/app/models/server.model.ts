export interface FhirServer {
    id: number;
    name: string;
    baseUrl: string;
}

export interface AddServerRequest {
    name: string;
    baseUrl: string;
}

/** Week 11: consolidated auth / isolation view per server (matches backend ServerAuthNarrativeResponse). */
export interface ServerAuthNarrative {
    serverId: number;
    serverName: string;
    baseUrl: string;
    oauthSmartAdvertised: boolean;
    anonymousPatientReadHttpStatus: number;
    authEnvironmentLabel: string;
    lastTestRunStartedAt: string | null;
    lastRunCrossPatientClassification: string;
    lastRunCrossPatientReason: string;
    lastRunOpenEndpointClassification: string;
    lastRunOpenEndpointReason: string;
    lastRunTokenIsolationClassification: string;
    lastRunTokenIsolationReason: string;
    lastRunObservationBundleClassification: string;
    lastRunObservationBundleReason: string;
    narrative: string;
}