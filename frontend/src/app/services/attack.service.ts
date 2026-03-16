import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RunResult, TestRun, TestRunSummary } from '../models/attack.model';

@Injectable({
    providedIn: 'root'
})
export class AttackService {
    private attacksUrl = 'http://localhost:8080/api/attacks';
    private resultsUrl = 'http://localhost:8080/api/results';

    constructor(private http: HttpClient) {}

    runAttacks(serverId: number): Observable<RunResult> {
        return this.http.post<RunResult>(`${this.attacksUrl}/run/${serverId}`, {});
    }

    getResults(testRunId: number): Observable<TestRun> {
        return this.http.get<TestRun>(`${this.resultsUrl}/${testRunId}`);
    }

    getRunsForServer(serverId: number): Observable<TestRunSummary[]> {
        return this.http.get<TestRunSummary[]>(`${this.attacksUrl}/runs/${serverId}`);
    }
}
