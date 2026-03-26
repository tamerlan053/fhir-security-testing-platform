import { Routes } from '@angular/router';
import { ServerManagementComponent } from './server-management/server-management';
import { AttackRunnerComponent } from './attack-runner/attack-runner';
import { ServerCompareComponent } from './server-compare/server-compare';

export const routes: Routes = [
    { path: '', redirectTo: 'servers', pathMatch: 'full' },
    { path: 'servers', component: ServerManagementComponent },
    { path: 'attacks', component: AttackRunnerComponent },
    { path: 'compare', component: ServerCompareComponent },
];
