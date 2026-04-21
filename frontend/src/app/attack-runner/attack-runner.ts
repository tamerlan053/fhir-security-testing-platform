import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AttackService } from '../services/attack.service';
import { ServerService } from '../services/server.service';
import { FhirServer } from '../models/server.model';
import { TestRun, TestResult } from '../models/attack.model';
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
          {{ getVulnerableCount() }} of {{ currentRun.results.length }} attacks classified as
          <strong>VULNERABLE</strong> (confirmed weakness)
        </p>
        <p class="summary validation-summary" *ngIf="getValidationResultCount() > 0">
          {{ getValidationVulnerableCount() }} of {{ getValidationResultCount() }} validation / injection scenarios
          <strong>VULNERABLE</strong>
        </p>
        <p class="summary covert-summary" *ngIf="getCovertChannelResultCount() > 0">
          {{ getCovertChannelVulnerableCount() }} of {{ getCovertChannelResultCount() }} covert-channel scenarios
          <strong>VULNERABLE</strong>
        </p>
        <p class="summary auth-summary" *ngIf="getAuthScenarioResultCount() > 0">
          {{ getAuthScenarioVulnerableCount() }} of {{ getAuthScenarioResultCount() }} authentication scenarios
          <strong>VULNERABLE</strong>
        </p>
        <p class="summary access-summary" *ngIf="getAccessControlResultCount() > 0">
          {{ getAccessControlVulnerableCount() }} of {{ getAccessControlResultCount() }} authorization / write scenarios
          <strong>VULNERABLE</strong>
        </p>
        <table class="results-table">
          <thead>
            <tr>
              <th>Attack</th>
              <th>Status</th>
              <th>Classification &amp; explanation</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let r of currentRun.results" [ngClass]="resultRowClass(r)">
              <td>{{ r.scenarioName }}</td>
              <td class="mono">{{ r.statusCode }}</td>
              <td class="classification-cell">
                <span class="badge" [ngClass]="badgeClass(r)">{{ r.classification }}</span>
                <span class="sev" *ngIf="r.severity">{{ r.severity }}</span>
                <div class="reason" [title]="r.reason">{{ truncateReason(r.reason) }}</div>
              </td>
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
    .validation-summary { font-size: 0.95em; color: #1a237e; }
    .access-summary { font-size: 0.95em; color: #37474f; }
    .covert-summary { font-size: 0.95em; color: #5d4037; }
    .auth-summary { font-size: 0.95em; color: #4a148c; }
    .results-table { width: 100%; border-collapse: collapse; margin-top: 12px; }
    .results-table th, .results-table td { border: 1px solid #ddd; padding: 10px 12px; text-align: left; vertical-align: top; }
    .results-table th { background: #f5f5f5; font-weight: 600; }
    .mono { font-family: monospace; }
    .classification-cell { min-width: 280px; }
    .badge { display: inline-block; padding: 3px 10px; border-radius: 4px; font-size: 0.72rem; font-weight: 700; letter-spacing: 0.02em; margin-right: 8px; }
    .sev { font-size: 0.72rem; color: #616161; font-weight: 600; }
    .reason { font-size: 0.82em; color: #444; margin-top: 6px; line-height: 1.35; max-width: 480px; }
    .badge-vulnerable { background: #ffcdd2; color: #b71c1c; }
    .badge-misconfigured { background: #ffe0b2; color: #e65100; }
    .badge-open-policy { background: #bbdefb; color: #0d47a1; }
    .badge-inconclusive { background: #eeeeee; color: #424242; }
    .badge-secure { background: #c8e6c9; color: #1b5e20; }
    .row-vulnerable { background: #fff5f5; border-left: 4px solid #c62828; }
    .row-misconfigured { background: #fff8f0; border-left: 4px solid #ef6c00; }
    .row-open-policy { background: #f3f9ff; border-left: 4px solid #1565c0; }
    .row-inconclusive { background: #fafafa; border-left: 4px solid #9e9e9e; }
    .row-secure { background: #f4fbf4; border-left: 4px solid #2e7d32; }
    .runs-section { margin-top: 24px; }
    .runs-list { list-style: none; padding: 0; }
    .runs-list li { margin: 6px 0; }
    .run-link { background: none; border: none; color: #1976d2; cursor: pointer; padding: 4px 0; text-align: left; }
    .run-link:hover { text-decoration: underline; }
  `]
})
export class AttackRunnerComponent implements OnInit {
  /** Must match Java {@code getName()} exactly — validation & malicious requests (1–3). */
  private readonly validationScenarioNames = [
    'Malformed JSON Request',
    'Metadata Manipulation',
    'Unexpected Payload Injection',
  ];

  /** Covert / hidden data (4–6). */
  private readonly covertChannelNames = [
    'Extension Fields Misuse',
    'Contained Resource Smuggling',
    'Encoded Hidden Data',
  ];

  /** Authentication (7–8). */
  private readonly authScenarioNames = [
    'Invalid Credentials Access Test',
    'Open Endpoint Detection',
  ];

  /** Authorization & unauthorized writes (9–10). */
  private readonly accessControlScenarioNames = [
    'Cross-Patient Access',
    'Unauthorized Write / ID Tampering',
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

  getValidationResultCount(): number {
    return this.currentRun?.results?.filter(r => this.validationScenarioNames.includes(r.scenarioName)).length ?? 0;
  }

  getValidationVulnerableCount(): number {
    return (
      this.currentRun?.results?.filter(
        r => this.validationScenarioNames.includes(r.scenarioName) && r.vulnerable,
      ).length ?? 0
    );
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

  getAuthScenarioResultCount(): number {
    return (
      this.currentRun?.results?.filter(r => this.authScenarioNames.includes(r.scenarioName)).length ?? 0
    );
  }

  getAuthScenarioVulnerableCount(): number {
    return (
      this.currentRun?.results?.filter(
        r => this.authScenarioNames.includes(r.scenarioName) && r.vulnerable,
      ).length ?? 0
    );
  }

  resultRowClass(r: TestResult): Record<string, boolean> {
    const c = r.classification ?? (r.vulnerable ? 'VULNERABLE' : 'SECURE');
    return {
      'row-vulnerable': c === 'VULNERABLE',
      'row-misconfigured': c === 'MISCONFIGURED',
      'row-open-policy': c === 'OPEN_POLICY',
      'row-inconclusive': c === 'INCONCLUSIVE',
      'row-secure': c === 'SECURE',
    };
  }

  badgeClass(r: TestResult): Record<string, boolean> {
    const key = (r.classification ?? (r.vulnerable ? 'VULNERABLE' : 'SECURE')).toLowerCase().replace(/_/g, '-');
    return {
      'badge-vulnerable': key === 'vulnerable',
      'badge-misconfigured': key === 'misconfigured',
      'badge-open-policy': key === 'open-policy',
      'badge-inconclusive': key === 'inconclusive',
      'badge-secure': key === 'secure',
    };
  }

  truncateReason(s: string | undefined, max = 140): string {
    if (!s) return '';
    const t = s.trim();
    return t.length <= max ? t : t.slice(0, max) + '…';
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
