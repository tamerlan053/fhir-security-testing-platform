import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ServerService } from '../services/server.service';
import { FhirServer, ServerAuthNarrative } from '../models/server.model';
import { formatApiError } from '../utils/error.utils';

@Component({
  selector: 'app-auth-strategy',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="nav">
        <a routerLink="/servers">← Servers</a>
      </div>

      <h2>Auth strategy &amp; isolation</h2>
      <p class="hint">
        Live metadata + anonymous /Patient probe; stored run excerpts for cross-patient,
        token isolation, and Observation bundle scenarios.
      </p>

      <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>
      <p *ngIf="loading" class="loading">Loading servers…</p>
      <p *ngIf="narrativeLoading" class="loading">Loading auth summaries…</p>

      <p *ngIf="!loading && servers.length === 0" class="muted">
        No servers configured. <a routerLink="/servers">Add servers</a> first, then run tests so last-run columns can populate.
      </p>

      <table *ngIf="!narrativeLoading && authRows.length > 0" class="auth-table">
        <thead>
          <tr>
            <th>Server</th>
            <th>OAuth/SMART (metadata)</th>
            <th>GET /Patient?_count=1</th>
            <th>Cross-patient (last run)</th>
            <th>Token isolation (last run)</th>
            <th>Obs. bundle dup. (last run)</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let row of authRows">
            <td>{{ row.serverName }}</td>
            <td>{{ row.oauthSmartAdvertised ? 'Advertised' : 'Not detected' }}</td>
            <td>HTTP {{ row.anonymousPatientReadHttpStatus }}</td>
            <td>{{ row.lastRunCrossPatientClassification || '—' }}</td>
            <td>{{ row.lastRunTokenIsolationClassification || '—' }}</td>
            <td>{{ row.lastRunObservationBundleClassification || '—' }}</td>
          </tr>
        </tbody>
      </table>

      <details *ngFor="let row of authRows" class="narrative-details">
        <summary>Narrative: {{ row.serverName }}</summary>
        <p class="narrative-body">{{ row.narrative }}</p>
      </details>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1100px; }
    .nav { margin-bottom: 16px; }
    .nav a { color: #1976d2; text-decoration: none; }
    .nav a:hover { text-decoration: underline; }
    h2 { margin-bottom: 8px; }
    .hint { color: #555; font-size: 14px; margin-bottom: 16px; max-width: 900px; line-height: 1.4; }
    .muted { color: #666; }
    .muted a { color: #1976d2; }
    .error { color: red; }
    .loading { color: #666; font-style: italic; margin-bottom: 12px; }
    .auth-table { width: 100%; border-collapse: collapse; font-size: 13px; margin-top: 8px; }
    .auth-table th, .auth-table td { border: 1px solid #ddd; padding: 8px; text-align: left; vertical-align: top; }
    .auth-table th { background: #f4f4f4; }
    .narrative-details { margin-top: 12px; }
    .narrative-body { white-space: pre-wrap; font-size: 13px; color: #333; margin: 8px 0 20px; padding: 10px; background: #fafafa; border: 1px solid #eee; border-radius: 4px; }
  `]
})
export class AuthStrategyComponent implements OnInit {
  servers: FhirServer[] = [];
  authRows: ServerAuthNarrative[] = [];
  loading = false;
  narrativeLoading = false;
  errorMessage = '';

  constructor(
    private serverService: ServerService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  private loadAll(): void {
    this.loading = true;
    this.narrativeLoading = false;
    this.errorMessage = '';
    this.authRows = [];
    this.serverService.getServers().subscribe({
      next: (data) => {
        this.servers = data;
        this.loading = false;
        this.loadAuthNarratives();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = formatApiError(err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private loadAuthNarratives(): void {
    if (this.servers.length === 0) {
      this.authRows = [];
      return;
    }
    this.narrativeLoading = true;
    forkJoin(
      this.servers.map((s) =>
        this.serverService.getAuthNarrative(s.id).pipe(
          catchError(() => of(null as ServerAuthNarrative | null))
        )
      )
    ).subscribe({
      next: (rows) => {
        this.authRows = rows.filter((r): r is ServerAuthNarrative => r != null);
        this.narrativeLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.narrativeLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
