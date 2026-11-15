import { inject } from '@angular/core';
import type { Routes } from '@angular/router';
import { formulierRoute, FORMULIER_VERSTUUR, veld, type Waarden } from '../formulieren/formulier';
import { GebruikersService } from './gebruikers.service';
import { ProfielPagina } from './profiel-pagina';

export const gebruikersRoutes: Routes = [
  {
    ...formulierRoute('login', {
      titel: 'Aanmelden',
      velden: [
        veld('username', 'Gebruikersnaam'),
        veld('password', 'Wachtwoord', 'password'),
      ],
      methode: 'POST',
      pad: '/login',
      terug: '/',
      herlaadNaOpslaan: true,
    }),
    providers: [{
      provide: FORMULIER_VERSTUUR,
      useFactory: () => {
        const gebruikers = inject(GebruikersService);
        return (body: Waarden) => gebruikers.aanmelden(
          String(body['username'] ?? ''),
          String(body['password'] ?? ''),
        );
      },
    }],
  },
  formulierRoute('registreer', {
    titel: 'Registreren',
    velden: [
      veld('voornaam', 'Voornaam'),
      veld('familienaam', 'Familienaam'),
      veld('username', 'Gebruikersnaam'),
      veld('password', 'Wachtwoord', 'password'),
      veld('confirmPassword', 'Herhaal wachtwoord', 'password'),
    ],
    methode: 'POST',
    pad: '/registreer',
    terug: '/login',
  }),
  { path: 'profiel', component: ProfielPagina },
];
