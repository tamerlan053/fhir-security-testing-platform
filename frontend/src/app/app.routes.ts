import { Routes } from '@angular/router';
import { ServerManagementComponent } from './server-management/server-management';

export const routes: Routes = [
    { path: '', redirectTo: 'servers', pathMatch: 'full' },
    { path: 'servers', component: ServerManagementComponent }
];
