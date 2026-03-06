import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { AddServerRequest, FhirServer } from "../models/server.model";

@Injectable({
    providedIn: 'root'
})
export class ServerService {
    private apiUrl = 'http://localhost:8080/api/servers';

    constructor(private http: HttpClient) {

    }

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