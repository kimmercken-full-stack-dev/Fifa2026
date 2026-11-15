import { TaalService, VertaalPipe } from '../shared/taal';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { foutmelding } from '../shared/api';
import { GebruikersService } from '../gebruikers/gebruikers.service';
import type { Profiel } from '../gebruikers/profiel';

@Component({
  imports: [RouterLink, RouterOutlet, VertaalPipe],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App implements OnInit {
  protected readonly taal = inject(TaalService);
  private readonly gebruikers = inject(GebruikersService);
  protected readonly profiel = signal<Profiel | null>(null);
  protected readonly fout = signal('');

  ngOnInit(): void {
    this.gebruikers.profiel().subscribe({
      next: (profiel) => this.profiel.set(profiel),
      error: (error) => {
        if (error.status !== 401) this.fout.set(foutmelding(error));
      },
    });
  }

  protected afmelden(): void {
    this.gebruikers.afmelden().subscribe({
      next: () => window.location.assign('/'),
      error: (error) => this.fout.set(foutmelding(error)),
    });
  }
}
