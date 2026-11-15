import { TaalService, VertaalPipe } from '../shared/taal';
import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, map, of, startWith, switchMap } from 'rxjs';
import { Api, foutmelding } from '../shared/api';
import { GebruikersService } from '../gebruikers/gebruikers.service';
import { Wedstrijdprognose } from '../prognoses/wedstrijdprognose';
import type { Wedstrijd } from './model';

type WedstrijdDetail = Wedstrijd & {
  capaciteit: number;
  winnaar: string | null;
  magVoorspellen: boolean;
  magUitslagInvullen: boolean;
};

@Component({
  selector: 'app-wedstrijddetail',
  imports: [DatePipe, RouterLink, VertaalPipe, Wedstrijdprognose],
  templateUrl: './wedstrijddetail.html',
})
export class Wedstrijddetail {
  private readonly taal = inject(TaalService);
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(Api);
  private readonly router = inject(Router);
  protected readonly profiel = toSignal(inject(GebruikersService).profiel().pipe(catchError(() => of(null))));
  protected readonly fout = signal('');
  protected readonly bezig = signal(false);

  protected verwijder(id: number): void {
    if (this.bezig() || !window.confirm(this.taal.vertaal('Wil je deze wedstrijd verwijderen?'))) return;
    this.bezig.set(true);
    this.fout.set('');
    this.api.schrijf('DELETE', '/beheer/wedstrijden/' + id).subscribe({
      next: () => void this.router.navigateByUrl('/'),
      error: (error) => {
        this.fout.set(foutmelding(error));
        this.bezig.set(false);
      },
    });
  }

  protected readonly detail = toSignal(this.route.paramMap.pipe(
    switchMap((params) => {
      const id = Number(params.get('id'));
      return this.api.get<WedstrijdDetail>('/wedstrijden/' + id).pipe(
        map((wedstrijd) => ({ wedstrijd, melding: '' })),
        startWith({ wedstrijd: undefined, melding: 'Wedstrijd ophalen…' }),
        catchError((error) => {
          if (error.status === 401) {
            void this.router.navigateByUrl('/login', { replaceUrl: true });
          }
          return of({ wedstrijd: undefined, melding: foutmelding(error) });
        }),
      );
    }),
  ), { requireSync: true });
}
