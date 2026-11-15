import type { Routes } from '@angular/router';
import { formulierRoute, veld } from '../formulieren/formulier';
import { TeamsPagina } from './teams-pagina';

export const teamsRoutes: Routes = [
  formulierRoute('teams/nieuw', { titel: 'Team aanmaken', velden: [veld('teamNaam', 'Teamnaam')], methode: 'POST', pad: '/teams', terug: '/mijn-teams' }),
  formulierRoute('teams/aansluiten', { titel: 'Aansluiten bij een team', velden: [veld('code', 'Uitnodigingscode', 'number')], methode: 'POST', pad: '/teams/join', terug: '/mijn-teams' }),
  { path: 'teams/:id/klassement', component: TeamsPagina, data: { soort: 'klassement', titel: 'Teamklassement' } },
  { path: 'teams/:id', component: TeamsPagina, data: { soort: 'team', titel: 'Teampagina' } },
  { path: 'teams', component: TeamsPagina, data: { soort: 'teams', titel: 'Teamoverzicht' } },
  { path: 'mijn-teams', component: TeamsPagina, data: { soort: 'mijn-teams', titel: 'Mijn teams' } },
];
