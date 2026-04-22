import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ServerService } from '../services/server.service';
import { FhirServer, AddServerRequest } from '../models/server.model';
import { formatApiError } from '../utils/error.utils';

@Component({
  selector: 'app-server-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container">
      <h2>FHIR Server Management</h2>
      <div class="nav">
        <a routerLink="/attacks">Run Security Tests →</a>
        <a routerLink="/compare">Compare servers →</a>
      </div>

      <form (ngSubmit)="onAdd()" class="form">
        <input [(ngModel)]="form.name" name="name" placeholder="Server name" required />
        <input [(ngModel)]="form.baseUrl" name="baseUrl" placeholder="Base URL (e.g. http://hapi.fhir.org/baseR4)" required />
        <button type="submit">Add Server</button>
      </form>

      <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>
      <p *ngIf="successMessage" class="success">{{ successMessage }}</p>

      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Base URL</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let server of servers">
            <td>{{ server.name }}</td>
            <td>{{ server.baseUrl }}</td>
            <td>
              <a routerLink="/attacks" [queryParams]="{serverId: server.id}" class="btn-link">Run Test</a>
              <button (click)="onDelete(server.id)">Delete</button>
            </td>
          </tr>
          <tr *ngIf="!loading && servers.length === 0">
            <td colspan="3">No servers yet. Add one above.</td>
          </tr>
          <tr *ngIf="loading">
            <td colspan="3" class="loading">Loading servers...</td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 800px; }
    .nav { margin-bottom: 16px; display: flex; gap: 16px; flex-wrap: wrap; }
    .nav a { color: #1976d2; text-decoration: none; }
    .nav a:hover { text-decoration: underline; }
    .form { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; }
    .form input { flex: 1; min-width: 200px; padding: 8px; }
    .form button { padding: 8px 16px; }
    table { width: 100%; border-collapse: collapse; }
    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
    th { background: #f4f4f4; }
    .error { color: red; }
    .success { color: green; }
    .loading { color: #666; font-style: italic; }
    .btn-link { color: #1976d2; margin-right: 8px; }
  `]
})
export class ServerManagementComponent implements OnInit {
  servers: FhirServer[] = [];
  form: AddServerRequest = { name: '', baseUrl: '' };
  errorMessage = '';
  successMessage = '';
  loading = false;

  constructor(
    private serverService: ServerService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadServers();
  }

  loadServers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.serverService.getServers().subscribe({
      next: (data) => {
        this.servers = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = formatApiError(err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onAdd(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.serverService.addServer(this.form).subscribe({
      next: () => {
        this.successMessage = 'Server added successfully';
        this.form = { name: '', baseUrl: '' };
        this.loadServers();
      },
      error: (err) => {
        this.errorMessage = formatApiError(err);
        this.cdr.detectChanges();
      }
    });
  }

  onDelete(id: number): void {
    if (!confirm('Delete this server?')) return;
    this.errorMessage = '';
    this.serverService.deleteServer(id).subscribe({
      next: () => this.loadServers(),
      error: (err) => {
        this.errorMessage = formatApiError(err);
        this.cdr.detectChanges();
      }
    });
  }
}