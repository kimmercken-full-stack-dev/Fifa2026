import type { Routes } from '@angular/router';
import { formulierRoute, veld } from '../formulieren/formulier';
import { PrognosesPagina } from './prognoses-pagina';

export const prognosesRoutes: Routes = [
  formulierRoute('prognoses/:id', {
    titel: 'Wedstrijd voorspellen',
    velden: [veld('uitslagLand1', 'Doelpunten land 1', 'number'), veld('uitslagLand2', 'Doelpunten land 2', 'number')],
    methode: 'PUT',
    pad: '/prognoses/:id',
    ophalen: '/prognoses/:id',
    terug: '/prognoses',
    bereidBodyVoor: (body, id) => { body['wedstrijdId'] = Number(id); },
  }),
  { path: 'prognoses', component: PrognosesPagina },
];
