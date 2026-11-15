import type { Routes } from '@angular/router';
import { formulierRoute, veld } from '../formulieren/formulier';
import { Wedstrijddetail } from './wedstrijddetail';
import { Wedstrijdoverzicht } from './wedstrijdoverzicht';

const scores = [veld('uitslagLand1', 'Doelpunten land 1', 'number'), veld('uitslagLand2', 'Doelpunten land 2', 'number')];
const wedstrijd = [
  veld('land1', 'Land 1'), veld('land2', 'Land 2'), veld('speeldatum', 'Speeldatum', 'date'),
  veld('starttijd', 'Starttijd', 'time'), veld('locatieId', 'Stadion', 'select'),
];

export const wedstrijdenRoutes: Routes = [
  { path: '', pathMatch: 'full', component: Wedstrijdoverzicht },
  { path: 'wedstrijd/:id', component: Wedstrijddetail },
  formulierRoute('beheer/wedstrijden/nieuw', { titel: 'Wedstrijd aanmaken', velden: wedstrijd, methode: 'POST', pad: '/beheer/wedstrijden', locaties: true, terug: '/' }),
  formulierRoute('beheer/wedstrijden/:id/wijzigen', {
    titel: 'Wedstrijd wijzigen', velden: wedstrijd, methode: 'PUT', pad: '/beheer/wedstrijden/:id',
    ophalen: '/beheer/wedstrijden/:id', locaties: true, terug: '/wedstrijd/:id',
    bereidWaardenVoor: (waarden) => {
      if (typeof waarden['starttijd'] === 'string') waarden['starttijd'] = waarden['starttijd'].slice(0, 5);
    },
    bereidBodyVoor: (body) => {
      if (typeof body['starttijd'] === 'string') body['starttijd'] += ':00';
    },
  }),
  formulierRoute('beheer/wedstrijden/:id/uitslag', { titel: 'Uitslag invoeren', velden: scores, methode: 'PUT', pad: '/beheer/wedstrijden/:id/uitslag', ophalen: '/beheer/wedstrijden/:id', terug: '/wedstrijd/:id' }),
];
