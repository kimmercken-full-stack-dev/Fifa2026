import { VertaalPipe } from '../shared/taal';
import { Component, DestroyRef, inject, InjectionToken, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Route, Router, RouterLink } from '@angular/router';
import { forkJoin, Observable, of } from 'rxjs';
import { Api, foutmelding } from '../shared/api';

type Veld = { naam: string; label: string; type: string };
export type Waarden = Record<string, string | number | null>;
type Locatie = { id: number; stadion: string; stad: string };
export type Formulier = {
  titel: string; velden: Veld[]; methode: 'POST' | 'PUT'; pad: string;
  ophalen?: string; locaties?: boolean; terug: string;
  bereidWaardenVoor?: (waarden: Waarden) => void;
  bereidBodyVoor?: (body: Waarden, id: string) => void;
  herlaadNaOpslaan?: boolean;
};

export const veld = (naam: string, label: string, type = 'text'): Veld => ({ naam, label, type });
export const FORMULIER_VERSTUUR = new InjectionToken<(body: Waarden) => Observable<unknown>>('formulier versturen');

export function formulierRoute(path: string, formulier: Formulier): Route {
  return { path, component: FormulierPagina, data: { formulier } };
}

@Component({
  imports: [FormsModule, RouterLink, VertaalPipe],
  templateUrl: './formulier.html',
  styles: `
    .invoerformulier {
      display: grid;
      gap: 0.5rem;
      max-width: 26rem;
      margin-bottom: 1rem;
    }
    input, select, button {
      font: inherit;
      padding: 0.5rem;
    }
  `,
})
export class FormulierPagina implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(Api);
  private readonly verstuur = inject(FORMULIER_VERSTUUR, { optional: true });
  private readonly destroyRef = inject(DestroyRef);
  private readonly id = this.route.snapshot.paramMap.get('id') ?? '';
  protected readonly formulier = this.route.snapshot.data['formulier'] as Formulier;
  protected readonly terug = this.formulier.terug.replace(':id', this.id);
  protected readonly fout = signal('');
  protected readonly bezig = signal(false);
  protected readonly klaar = signal(false);
  protected readonly locaties = signal<Locatie[]>([]);
  protected waarden: Waarden = {};

  ngOnInit(): void {
    const { ophalen, locaties } = this.formulier;
    forkJoin({
      waarden: ophalen ? this.api.get<Waarden>(ophalen.replace(':id', this.id)) : of<Waarden>({}),
      locaties: locaties ? this.api.get<Locatie[]>('/beheer/locaties') : of([]),
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => {
        this.waarden = { ...data.waarden };
        this.formulier.bereidWaardenVoor?.(this.waarden);
        this.locaties.set(data.locaties);
        this.klaar.set(true);
      },
      error: (error) => this.fout.set(foutmelding(error)),
    });
  }

  protected bewaar(): void {
    if (this.bezig()) return;
    const { pad, methode, velden } = this.formulier;
    const body = Object.fromEntries(velden.map(({ naam }) => [naam, this.waarden[naam]]));
    this.formulier.bereidBodyVoor?.(body, this.id);
    const verzoek = this.verstuur?.(body) ?? this.api.schrijf(methode, pad.replace(':id', this.id), body);
    this.bezig.set(true);
    this.fout.set('');
    verzoek.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        if (this.formulier.herlaadNaOpslaan) window.location.assign(this.terug);
        else void this.router.navigateByUrl(this.terug);
      },
      error: (error) => {
        this.fout.set(foutmelding(error));
        this.bezig.set(false);
      },
    });
  }
}
