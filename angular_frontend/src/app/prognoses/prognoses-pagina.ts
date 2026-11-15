import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Api, foutmelding } from '../shared/api';
import { TaalService, VertaalPipe } from '../shared/taal';

type Prognose = { id: number; wedstrijdId: number; land1: string; land2: string;
  speeldatum: string; starttijd: string; stad: string; stadion: string; winnaar: string | null;
  uitslagLand1: number; uitslagLand2: number; wijzigbaar: boolean };

@Component({ imports: [RouterLink, VertaalPipe], templateUrl: './prognoses-pagina.html' })
export class PrognosesPagina implements OnInit {
  private readonly api = inject(Api);
  private readonly taal = inject(TaalService);
  protected readonly prognoses = signal<Prognose[]>([]);
  protected readonly fout = signal('');
  protected readonly bezig = signal(false);

  ngOnInit(): void {
    this.laad();
  }

  private laad(): void {
    this.api.get<Prognose[]>('/prognoses').subscribe({
      next: this.prognoses.set,
      error: (error) => this.fout.set(foutmelding(error)),
    });
  }

  protected verwijder(id: number): void {
    if (this.bezig() || !window.confirm(this.taal.vertaal('Wil je dit verwijderen?'))) return;
    this.bezig.set(true);
    this.fout.set('');
    this.api.schrijf('DELETE', '/prognoses/' + id).subscribe({
      next: () => {
        this.bezig.set(false);
        this.laad();
      },
      error: (error) => {
        this.fout.set(foutmelding(error));
        this.bezig.set(false);
      },
    });
  }
}
