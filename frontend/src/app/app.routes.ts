import { Routes } from '@angular/router';
import { ServerManagementComponent } from './server-management/server-management';
import { AttackRunnerComponent } from './attack-runner/attack-runner';

export const routes: Routes = [
    { path: '', redirectTo: 'servers', pathMatch: 'full' },
    { path: 'servers', component: ServerManagementComponent },
    { path: 'attacks', component: AttackRunnerComponent }
];
