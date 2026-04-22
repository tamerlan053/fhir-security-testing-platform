export interface FhirServer {
    id: number;
    name: string;
    baseUrl: string;
}

export interface AddServerRequest {
    name: string;
    baseUrl: string;
}