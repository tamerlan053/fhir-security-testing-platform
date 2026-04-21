import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AttackService } from '../services/attack.service';
import { ServerService } from '../services/server.service';
import { FhirServer } from '../models/server.model';
import { CompareCell, CompareResponse } from '../models/compare.model';
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
                  {{ col.vulnerableCount }} / {{ col.resultCount }} VULNERABLE
                </div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let row of c.attacks" [class.empty-row]="c.attacks.length === 0">
              <td class="attack-col">{{ row.scenarioName }}</td>
              <td
                *ngFor="let cell of row.cells; let i = index"
                [ngClass]="cellClasses(cell)"
              >
                <ng-container *ngIf="cell.present">
                  <div class="cell-line"><span class="code">{{ cell.statusCode }}</span></div>
                  <div class="cell-line" *ngIf="cell.classification">
                    <span class="badge" [ngClass]="badgeClass(cell)">{{ cell.classification }}</span>
                  </div>
                  <div class="cell-reason" *ngIf="cell.reason" [title]="cell.reason">
                    {{ truncate(cell.reason, 56) }}
                  </div>
                </ng-container>
                <span *ngIf="!cell.present" class="missing">—</span>
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
    .matrix td.cell-vulnerable { background: #ffebee; }
    .matrix td.cell-misconfigured { background: #fff3e0; }
    .matrix td.cell-open-policy { background: #e3f2fd; }
    .matrix td.cell-inconclusive { background: #f5f5f5; }
    .matrix td.cell-secure { background: #e8f5e9; }
    .matrix td.missing-cell { color: #999; }
    .cell-line { margin-bottom: 4px; }
    .cell-reason { font-size: 0.78rem; color: #555; line-height: 1.25; max-width: 200px; }
    .code { font-family: monospace; }
    .badge { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 0.68rem; font-weight: 700; }
    .badge-vulnerable { background: #ffcdd2; color: #b71c1c; }
    .badge-misconfigured { background: #ffe0b2; color: #e65100; }
    .badge-open-policy { background: #bbdefb; color: #0d47a1; }
    .badge-inconclusive { background: #eeeeee; color: #424242; }
    .badge-secure { background: #c8e6c9; color: #1b5e20; }
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

  cellClasses(cell: CompareCell): Record<string, boolean> {
    if (!cell.present) {
      return { 'missing-cell': true };
    }
    const c = (cell.classification ?? (cell.vulnerable ? 'VULNERABLE' : 'SECURE')).toUpperCase();
    return {
      'cell-vulnerable': c === 'VULNERABLE',
      'cell-misconfigured': c === 'MISCONFIGURED',
      'cell-open-policy': c === 'OPEN_POLICY',
      'cell-inconclusive': c === 'INCONCLUSIVE',
      'cell-secure': c === 'SECURE',
    };
  }

  badgeClass(cell: CompareCell): Record<string, boolean> {
    const key = (cell.classification ?? (cell.vulnerable ? 'VULNERABLE' : 'SECURE')).toLowerCase().replace(/_/g, '-');
    return {
      'badge-vulnerable': key === 'vulnerable',
      'badge-misconfigured': key === 'misconfigured',
      'badge-open-policy': key === 'open-policy',
      'badge-inconclusive': key === 'inconclusive',
      'badge-secure': key === 'secure',
    };
  }

  truncate(s: string, max: number): string {
    if (!s) return '';
    return s.length <= max ? s : s.slice(0, max) + '…';
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
