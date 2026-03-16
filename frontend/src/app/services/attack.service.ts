import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RunResult, TestRun, TestRunSummary } from '../models/attack.model';

@Injectable({
    providedIn: 'root'
})
export class AttackService {
    private readonly baseUrl = environment.apiBaseUrl;

    constructor(private http: HttpClient) {}

    runAttacks(serverId: number): Observable<RunResult> {
        return this.http.post<RunResult>(`${this.baseUrl}/api/attacks/run/${serverId}`, {});
    }

    getResults(testRunId: number): Observable<TestRun> {
        return this.http.get<TestRun>(`${this.baseUrl}/api/results/${testRunId}`);
    }

    getRunsForServer(serverId: number): Observable<TestRunSummary[]> {
        return this.http.get<TestRunSummary[]>(`${this.baseUrl}/api/attacks/runs/${serverId}`);
    }
}
