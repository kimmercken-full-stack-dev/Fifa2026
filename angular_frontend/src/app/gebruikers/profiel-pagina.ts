import { Component, inject, OnInit, signal } from '@angular/core';
import { foutmelding } from '../shared/api';
import { VertaalPipe } from '../shared/taal';
import { GebruikersService } from './gebruikers.service';
import type { Profiel } from './profiel';

@Component({ imports: [VertaalPipe], templateUrl: './profiel-pagina.html' })
export class ProfielPagina implements OnInit {
  private readonly gebruikers = inject(GebruikersService);
  protected readonly profiel = signal<Profiel | null>(null);
  protected readonly fout = signal('');

  ngOnInit(): void {
    this.gebruikers.profiel().subscribe({
      next: this.profiel.set,
      error: (error) => this.fout.set(foutmelding(error)),
    });
  }
}
