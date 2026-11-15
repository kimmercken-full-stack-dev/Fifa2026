import { Component, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { catchError, distinctUntilChanged, map, of, switchMap, tap } from 'rxjs';
import { Api, foutmelding } from '../shared/api';
import { VertaalPipe } from '../shared/taal';
import type { Wedstrijd } from '../wedstrijden/model';

type Prognose = { uitslagLand1: number | null; uitslagLand2: number | null };

@Component({
  selector: 'app-wedstrijdprognose',
  imports: [VertaalPipe],
  templateUrl: './wedstrijdprognose.html',
})
export class Wedstrijdprognose {
  private readonly api = inject(Api);
  readonly wedstrijd = input.required<Pick<Wedstrijd, 'wedstrijdID' | 'land1' | 'land2'> & { magVoorspellen: boolean }>();
  protected readonly fout = signal('');
  protected readonly bezig = signal(false);
  protected readonly prognoseOpgeslagen = signal(false);
  protected readonly prognose = signal<Prognose | null>(null);
  constructor() {
    toObservable(this.wedstrijd).pipe(
      map((wedstrijd) => wedstrijd.wedstrijdID),
      distinctUntilChanged(),
      tap(() => {
        this.prognose.set(null);
        this.prognoseOpgeslagen.set(false);
      }),
      switchMap((id) => this.api.get<Prognose>('/prognoses/' + id).pipe(
        catchError(() => of(null)),
      )),
      takeUntilDestroyed(),
    ).subscribe(this.prognose.set);
  }

  protected bewaarPrognose(id: number, invoer: Prognose): void {
    if (this.bezig()) return;
    this.bezig.set(true);
    this.fout.set('');
    this.prognoseOpgeslagen.set(false);
    this.api.schrijf('PUT', '/prognoses/' + id, { ...invoer, wedstrijdId: id }).subscribe({
      next: () => {
        this.prognose.set(invoer);
        this.prognoseOpgeslagen.set(true);
        this.bezig.set(false);
      },
      error: (error) => {
        this.fout.set(foutmelding(error));
        this.bezig.set(false);
      },
    });
  }
}
