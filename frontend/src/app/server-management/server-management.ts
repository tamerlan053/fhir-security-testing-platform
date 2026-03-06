import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ServerService } from '../services/server.service';
import { FhirServer, AddServerRequest } from '../models/server.model';

@Component({
  selector: 'app-server-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <h2>FHIR Server Management</h2>

      <form (ngSubmit)="onAdd()" class="form">
        <input [(ngModel)]="form.name" name="name" placeholder="Server name" required />
        <input [(ngModel)]="form.baseUrl" name="baseUrl" placeholder="Base URL (e.g. <http://hapi.fhir.org/baseR4>)" required />
        <button type="submit">Add Server</button>
      </form>

      <p *ngIf="errorMessage" class="error">{{ errorMessage }}</p>
      <p *ngIf="successMessage" class="success">{{ successMessage }}</p>

      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Base URL</th>
            <th>Auth</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let server of servers">
            <td>{{ server.name }}</td>
            <td>{{ server.baseUrl }}</td>
            <td>{{ server.authenticationType || '-' }}</td>
            <td>
              <button (click)="onDelete(server.id)">Delete</button>
            </td>
          </tr>
          <tr *ngIf="servers.length === 0">
            <td colspan="4">No servers yet. Add one above.</td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .container { padding: 20px; max-width: 800px; }
    .form { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; }
    .form input { flex: 1; min-width: 200px; padding: 8px; }
    .form button { padding: 8px 16px; }
    table { width: 100%; border-collapse: collapse; }
    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
    th { background: #f4f4f4; }
    .error { color: red; }
    .success { color: green; }
  `]
})
export class ServerManagementComponent implements OnInit {
  servers: FhirServer[] = [];
  form: AddServerRequest = { name: '', baseUrl: '', authenticationType: '' };
  errorMessage = '';
  successMessage = '';

  constructor(private serverService: ServerService) {}

  ngOnInit(): void {
    this.loadServers();
  }

  loadServers(): void {
    this.serverService.getServers().subscribe({
      next: (data) => this.servers = data,
      error: (err) => this.errorMessage = 'Failed to load servers'
    });
  }

  onAdd(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.serverService.addServer(this.form).subscribe({
      next: () => {
        this.successMessage = 'Server added successfully';
        this.form = { name: '', baseUrl: '', authenticationType: '' };
        this.loadServers();
      },
      error: (err) => this.errorMessage = err.error?.message || 'Failed to add server'
    });
  }

  onDelete(id: number): void {
    if (!confirm('Delete this server?')) return;
    this.errorMessage = '';
    this.serverService.deleteServer(id).subscribe({
      next: () => this.loadServers(),
      error: (err) => this.errorMessage = err.error?.message || 'Failed to delete'
    });
  }
}