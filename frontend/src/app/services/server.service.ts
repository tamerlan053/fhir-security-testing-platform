import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { AddServerRequest, FhirServer } from "../models/server.model";

@Injectable({
    providedIn: 'root'
})
export class ServerService {
    private readonly apiUrl = `${environment.apiBaseUrl}/api/servers`;

    constructor(private http: HttpClient) {}

    getServers(): Observable<FhirServer[]> {
        return this.http.get<FhirServer[]>(this.apiUrl);
    }

    addServer(request: AddServerRequest): Observable<FhirServer> {
        return this.http.post<FhirServer>(this.apiUrl, request);
    }

    deleteServer(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}