import type { Routes } from '@angular/router';
import { Foutpagina } from '../shared/foutpagina';
import { gebruikersRoutes } from '../gebruikers/gebruikers.routes';
import { prognosesRoutes } from '../prognoses/prognoses.routes';
import { teamsRoutes } from '../teams/teams.routes';
import { wedstrijdenRoutes } from '../wedstrijden/wedstrijden.routes';

export const routes: Routes = [
  ...wedstrijdenRoutes,
  ...gebruikersRoutes,
  ...prognosesRoutes,
  ...teamsRoutes,
  { path: '**', component: Foutpagina },
];
