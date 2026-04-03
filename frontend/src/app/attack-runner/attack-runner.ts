import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AttackService } from '../services/attack.service';
import { ServerService } from '../services/server.service';
import { FhirServer } from '../models/server.model';
import { TestRun } from '../models/attack.model';
import { formatApiError } from '../utils/error.utils';

@Component({
  selector: 'app-attack-runner',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container">
      <h2>Security Test Runner</h2>
      <div class="nav">
        <a routerLink="/servers">← Back to Servers</a>
        <a routerLink="/compare">Compare results</a>
      </div>

      <div class="controls">
        <div class="control-group">
          <label for="server">Select Server:</label>
          <select id="server" [(ngModel)]="selectedServerId" (ngModelChange)="onServerChange()">
            <option [ngValue]="null" disabled>-- Select server --</option>
            <option *ngFor="let s of servers" [ngValue]="s.id">{{ s.name }} ({{ s.baseUrl }})</option>
          </select>
        </div>
        <button (click)="runAttacks()" [disabled]="!selectedServerId || running">
          {{ running ? 'Running...' : 'Run Security Test' }}
        </button>
      </div>

      <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>
      <p *ngIf="successMessage" class="success">{{ successMessage }}</p>

      <div class="results-section" *ngIf="currentRun">
        <h3>Results — {{ currentRun.serverName }} ({{ currentRun.startedAt }})</h3>
        <p class="summary" *ngIf="currentRun.results.length > 0">
          {{ getVulnerableCount() }} of {{ currentRun.results.length }} attacks vulnerable
        </p>
        <p class="summary access-summary" *ngIf="getAccessControlResultCount() > 0">
          {{ getAccessControlVulnerableCount() }} of {{ getAccessControlResultCount() }} access-control attacks vulnerable
        </p>
        <p class="summary covert-summary" *ngIf="getCovertChannelResultCount() > 0">
          {{ getCovertChannelVulnerableCount() }} of {{ getCovertChannelResultCount() }} covert channel attacks allow hidden data
        </p>
        <table class="results-table">
          <thead>
            <tr>
              <th>Attack</th>
              <th>Status Code</th>
              <th>Vulnerable</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of currentRun.results" [class.vulnerable]="r.vulnerable">
              <td>{{ r.scenarioName }}</td>
              <td>{{ r.statusCode }}</td>
              <td>{{ r.vulnerable ? '⚠ Vulnerable' : '✓ OK' }}</td>
            </tr>
            <tr *ngIf="currentRun.results.length === 0">
              <td colspan="3">No results yet.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="runs-section" *ngIf="previousRuns.length > 0">
        <h3>Previous Runs</h3>
        <ul class="runs-list">
          <li *ngFor="let run of previousRuns">
            <button (click)="loadRun(run.testRunId)" class="run-link">
              Run #{{ run.testRunId }} — {{ run.startedAt }}
            </button>
          </li>
        </ul>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 900px; }
    .nav { margin-bottom: 20px; display: flex; gap: 16px; flex-wrap: wrap; align-items: center; }
    .nav a { color: #1976d2; text-decoration: none; }
    .nav a:hover { text-decoration: underline; }
    .controls { display: flex; gap: 16px; align-items: flex-end; margin-bottom: 20px; flex-wrap: wrap; }
    .control-group { display: flex; flex-direction: column; gap: 4px; }
    .control-group label { font-size: 0.9em; color: #555; }
    .control-group select { min-width: 280px; padding: 8px 12px; }
    button { padding: 10px 20px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
    button:hover:not(:disabled) { background: #1565c0; }
    button:disabled { opacity: 0.6; cursor: not-allowed; }
    .error { color: #c62828; }
    .success { color: #2e7d32; }
    .results-section { margin-top: 24px; }
    .summary { margin: 8px 0; font-weight: 500; color: #333; }
    .access-summary { font-size: 0.95em; color: #37474f; }
    .covert-summary { font-size: 0.95em; color: #5d4037; }
    .results-table { width: 100%; border-collapse: collapse; margin-top: 12px; }
    .results-table th, .results-table td { border: 1px solid #ddd; padding: 10px 12px; text-align: left; }
    .results-table th { background: #f5f5f5; font-weight: 600; }
    .results-table tr.vulnerable { background: #ffebee; }
    .runs-section { margin-top: 24px; }
    .runs-list { list-style: none; padding: 0; }
    .runs-list li { margin: 6px 0; }
    .run-link { background: none; border: none; color: #1976d2; cursor: pointer; padding: 4px 0; text-align: left; }
    .run-link:hover { text-decoration: underline; }
  `]
})
export class AttackRunnerComponent implements OnInit {
  private readonly covertChannelNames = [
    'Extension Fields Misuse',
    'Manipulated Identifiers',
    'Embedded Contained Resources',
    'Unexpected JSON Fragments',
    'Encoded Hidden Data',
  ];

  private readonly accessControlScenarioNames = [
    'Cross-patient Access',
    'Owner/Reference Manipulation',
    'ID Tampering',
    'Unauthorized Resource Retrieval',
  ];

  servers: FhirServer[] = [];
  selectedServerId: number | null = null;
  currentRun: TestRun | null = null;
  previousRuns: { testRunId: number; startedAt: string }[] = [];
  running = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private attackService: AttackService,
    private serverService: ServerService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadServers();
    this.route.queryParams.subscribe(params => {
      const serverId = params['serverId'];
      if (serverId) {
        this.selectedServerId = +serverId;
        this.onServerChange();
      }
    });
  }

  loadServers(): void {
    this.errorMessage = '';
    this.serverService.getServers().subscribe({
      next: (data) => {
        this.servers = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = formatApiError(err);
        this.cdr.detectChanges();
      }
    });
  }

  onServerChange(): void {
    this.currentRun = null;
    if (this.selectedServerId) {
      this.attackService.getRunsForServer(this.selectedServerId).subscribe({
        next: (runs) => {
          this.previousRuns = runs;
          this.cdr.detectChanges();
        },
        error: () => {
          this.previousRuns = [];
          this.cdr.detectChanges();
        }
      });
    } else {
      this.previousRuns = [];
    }
  }

  runAttacks(): void {
    if (!this.selectedServerId) return;
    this.errorMessage = '';
    this.successMessage = '';
    this.running = true;

    this.attackService.runAttacks(this.selectedServerId).subscribe({
      next: (result) => {
        this.successMessage = `Test run completed. Run ID: ${result.testRunId}`;
        this.loadRun(result.testRunId);
        this.onServerChange();
        this.running = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = formatApiError(err);
        this.running = false;
        this.cdr.detectChanges();
      }
    });
  }

  getVulnerableCount(): number {
    return this.currentRun?.results?.filter(r => r.vulnerable).length ?? 0;
  }

  getCovertChannelResultCount(): number {
    return this.currentRun?.results?.filter(r => this.covertChannelNames.includes(r.scenarioName)).length ?? 0;
  }

  getCovertChannelVulnerableCount(): number {
    return (
      this.currentRun?.results?.filter(
        r => this.covertChannelNames.includes(r.scenarioName) && r.vulnerable,
      ).length ?? 0
    );
  }

  getAccessControlResultCount(): number {
    return this.currentRun?.results?.filter(r =>
      this.accessControlScenarioNames.includes(r.scenarioName),
    ).length ?? 0;
  }

  getAccessControlVulnerableCount(): number {
    return (
      this.currentRun?.results?.filter(
        r => this.accessControlScenarioNames.includes(r.scenarioName) && r.vulnerable,
      ).length ?? 0
    );
  }

  loadRun(testRunId: number): void {
    this.attackService.getResults(testRunId).subscribe({
      next: (run) => {
        this.currentRun = run;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = formatApiError(err);
        this.cdr.detectChanges();
      }
    });
  }
}
