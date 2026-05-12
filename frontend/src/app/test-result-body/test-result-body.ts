import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AttackService } from '../services/attack.service';
import { LeakageExposure, TestResult } from '../models/attack.model';
import { formatApiError } from '../utils/error.utils';

@Component({
  selector: 'app-test-result-body',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="nav">
        <a routerLink="/attacks" [queryParams]="backToRunnerQuery">← Back to Security Test Runner</a>
      </div>

      <p *ngIf="loading" class="muted">Loading…</p>
      <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>

      <div *ngIf="result as r" class="panel">
        <h2>Response body</h2>
        <p class="meta">
          <strong>{{ r.scenarioName }}</strong>
          · HTTP {{ r.statusCode }}
          · <span class="mono">{{ r.classification }}</span>
          <span *ngIf="r.severity"> · {{ r.severity }}</span>
        </p>
        <p class="meta muted" *ngIf="r.attackVectorIds">Vectors: <span class="mono">{{ r.attackVectorIds }}</span></p>
        <p class="meta muted" *ngIf="r.leakageExposure">Leakage: {{ r.leakageExposure }}</p>
        <div class="actions">
          <button type="button" class="btn-secondary" (click)="copyBody()" [disabled]="!r.responseBody">
            Copy to clipboard
          </button>
        </div>
        <p *ngIf="copyMessage" class="success">{{ copyMessage }}</p>
        <pre class="body-block">{{ r.responseBody || '(empty)' }}</pre>
      </div>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 1100px; margin: 0 auto; }
    .nav { margin-bottom: 16px; }
    .nav a { color: #1976d2; text-decoration: none; }
    .nav a:hover { text-decoration: underline; }
    .muted { color: #666; }
    .error { color: #c62828; }
    .success { color: #2e7d32; font-size: 0.9em; }
    .panel { margin-top: 8px; }
    .meta { margin: 6px 0; line-height: 1.4; }
    .mono { font-family: monospace; font-size: 0.88em; }
    .actions { margin: 12px 0; }
    .btn-secondary { padding: 8px 14px; background: #eceff1; color: #37474f; border: 1px solid #cfd8dc; border-radius: 4px; cursor: pointer; }
    .btn-secondary:hover:not(:disabled) { background: #dfe4e7; }
    .btn-secondary:disabled { opacity: 0.5; cursor: not-allowed; }
    .body-block {
      margin-top: 12px;
      padding: 16px;
      background: #fafafa;
      border: 1px solid #e0e0e0;
      border-radius: 6px;
      font-family: ui-monospace, Consolas, monospace;
      font-size: 0.82rem;
      white-space: pre-wrap;
      word-break: break-word;
      max-height: 75vh;
      overflow: auto;
    }
  `],
})
export class TestResultBodyComponent implements OnInit {
  result: TestResult | null = null;
  loading = true;
  errorMessage = '';
  copyMessage = '';
  /** When opened from a run row, preserves testRunId + serverId so Back restores the same run. */
  backToRunnerQuery: Record<string, string> = {};

  constructor(
    private route: ActivatedRoute,
    private attackService: AttackService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const q = this.route.snapshot.queryParamMap;
    const tr = q.get('testRunId');
    const sr = q.get('serverId');
    if (tr) {
      this.backToRunnerQuery['testRunId'] = tr;
    }
    if (sr) {
      this.backToRunnerQuery['serverId'] = sr;
    }

    const idParam = this.route.snapshot.paramMap.get('testResultId');
    const id = idParam ? parseInt(idParam, 10) : NaN;
    if (Number.isNaN(id)) {
      this.loading = false;
      this.errorMessage = 'Invalid test result id.';
      return;
    }

    this.attackService.getTestResultById(id).subscribe({
      next: (r) => {
        this.result = {
          ...r,
          attackVectorIds: r.attackVectorIds ?? '',
          leakageExposure: (r.leakageExposure ?? 'NONE') as LeakageExposure,
        };
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

  copyBody(): void {
    const text = this.result?.responseBody ?? '';
    if (!text) {
      return;
    }
    this.copyMessage = '';
    navigator.clipboard.writeText(text).then(
      () => {
        this.copyMessage = 'Copied.';
        this.cdr.detectChanges();
      },
      () => {
        this.copyMessage = 'Copy failed (browser permission).';
        this.cdr.detectChanges();
      },
    );
  }
}
