export interface FhirServer {
    id: number;
    name: string;
    baseUrl: string;
    authenticationType: string | null;
}

export interface AddServerRequest {
    name: string;
    baseUrl: string;
    authenticationType?: string;
}