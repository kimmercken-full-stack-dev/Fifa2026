import { VertaalPipe } from './taal';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  imports: [RouterLink, VertaalPipe],
  template: `<h1>{{ 'Pagina niet gevonden' | vertaal }}</h1><a routerLink="/">{{ 'Naar het wedstrijdoverzicht' | vertaal }}</a>`,
})
export class Foutpagina {}
