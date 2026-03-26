import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AttackService } from '../services/attack.service';
import { ServerService } from '../services/server.service';
import { FhirServer } from '../models/server.model';
import { CompareResponse } from '../models/compare.model';
import { formatApiError } from '../utils/error.utils';

@Component({
  selector: 'app-server-compare',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container">
      <h2>Compare Results Across Servers</h2>
      <p class="intro">
        Uses each server’s <strong>latest</strong> test run. Run tests from
        <a routerLink="/attacks">Security Test Runner</a> first.
      </p>
      <div class="nav">
        <a routerLink="/servers">← Servers</a>
        <a routerLink="/attacks">Run tests</a>
      </div>

      <div class="picker" *ngIf="servers.length > 0">
        <p class="label">Select servers:</p>
        <ul class="check-list">
          <li *ngFor="let s of servers">
            <label>
              <input
                type="checkbox"
                [checked]="selectedIds.has(s.id)"
                (change)="toggle(s.id)"
              />
              {{ s.name }} <span class="muted">({{ s.baseUrl }})</span>
            </label>
          </li>
        </ul>
        <button type="button" (click)="loadComparison()" [disabled]="selectedIds.size === 0 || loading">
          {{ loading ? 'Loading…' : 'Load comparison' }}
        </button>
      </div>

      <p *ngIf="!loading && servers.length === 0" class="muted">No servers configured. Add one on the servers page.</p>
      <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>

      <div class="results" *ngIf="comparison as c">
        <h3>Comparison matrix</h3>
        <table class="matrix">
          <thead>
            <tr>
              <th class="attack-col">Attack</th>
              <th *ngFor="let col of c.servers">
                <div class="hdr-name">{{ col.serverName }}</div>
                <div class="hdr-meta">
                  <span *ngIf="col.testRunId !== null">Run #{{ col.testRunId }}</span>
                  <span *ngIf="col.testRunId === null" class="warn">No run yet</span>
                </div>
                <div class="hdr-stats" *ngIf="col.resultCount > 0">
                  {{ col.vulnerableCount }} / {{ col.resultCount }} vulnerable
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let row of c.attacks" [class.empty-row]="c.attacks.length === 0">
              <td class="attack-col">{{ row.scenarioName }}</td>
              <td
                *ngFor="let cell of row.cells; let i = index"
                [class.vulnerable]="cell.present && cell.vulnerable"
                [class.missing]="!cell.present"
              >
                <ng-container *ngIf="cell.present">
                  <span class="code">{{ cell.statusCode }}</span>
                  <span class="flag">{{ cell.vulnerable ? '⚠ Vulnerable' : '✓ OK' }}</span>
                </ng-container>
                <span *ngIf="!cell.present">—</span>
              </td>
            </tr>
            <tr *ngIf="c.attacks.length === 0">
              <td [attr.colspan]="Math.max(1, c.servers.length + 1)" class="muted">
                No attack results in the selected runs. Run a full test for at least one server.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1200px; }
    .intro { color: #444; margin-bottom: 12px; }
    .nav { display: flex; gap: 16px; margin-bottom: 20px; }
    .nav a { color: #1976d2; text-decoration: none; }
    .nav a:hover { text-decoration: underline; }
    .picker { margin-bottom: 24px; }
    .label { font-weight: 600; margin-bottom: 8px; }
    .check-list { list-style: none; padding: 0; margin: 0 0 12px; }
    .check-list li { margin: 6px 0; }
    .muted { color: #666; font-size: 0.9em; }
    .warn { color: #e65100; }
    button { padding: 10px 20px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
    button:disabled { opacity: 0.6; cursor: not-allowed; }
    .error { color: #c62828; }
    .results { margin-top: 24px; }
    .matrix { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
    .matrix th, .matrix td { border: 1px solid #ddd; padding: 10px 12px; vertical-align: top; }
    .matrix th { background: #f5f5f5; text-align: left; }
    .attack-col { min-width: 200px; font-weight: 500; background: #fafafa; }
    .hdr-name { font-weight: 600; }
    .hdr-meta, .hdr-stats { font-size: 0.8em; color: #555; margin-top: 4px; }
    .matrix td.vulnerable { background: #ffebee; }
    .matrix td.missing { color: #999; }
    .code { font-family: monospace; margin-right: 8px; }
    .flag { white-space: nowrap; }
  `],
})
export class ServerCompareComponent implements OnInit {
  readonly Math = Math;

  servers: FhirServer[] = [];
  selectedIds = new Set<number>();
  comparison: CompareResponse | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private attackService: AttackService,
    private serverService: ServerService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.serverService.getServers().subscribe({
      next: (list) => {
        this.servers = list;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = formatApiError(err);
        this.cdr.detectChanges();
      },
    });
  }

  toggle(id: number): void {
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
    } else {
      this.selectedIds.add(id);
    }
    this.selectedIds = new Set(this.selectedIds);
  }

  loadComparison(): void {
    const ids = [...this.selectedIds].sort((a, b) => a - b);
    if (ids.length === 0) return;
    this.errorMessage = '';
    this.loading = true;
    this.comparison = null;
    this.attackService.getComparison(ids).subscribe({
      next: (data) => {
        this.comparison = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = formatApiError(err);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }
}
