import { TaalService, VertaalPipe } from '../shared/taal';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Api, foutmelding } from '../shared/api';
import { GebruikersService } from '../gebruikers/gebruikers.service';
import type { Profiel } from '../gebruikers/profiel';

type Team = { teamId: number; teamNaam: string; eigenaar: string; score: number; aantalLeden: number };
type Lid = { id: number; username: string; score: number; eigenaar: boolean };
type TeamDetail = { teamNaam: string; eigenaar: boolean; invitecode: number | null; leden: Lid[] };
type Klassement = { teamNaam: string; teamScore: number;
  leden: { positie: number; nickname: string; score: number }[] };

@Component({ imports: [RouterLink, VertaalPipe], templateUrl: './teams-pagina.html' })
export class TeamsPagina implements OnInit {
  private readonly taal = inject(TaalService);
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(Api);
  private readonly gebruikers = inject(GebruikersService);
  private readonly router = inject(Router);
  protected readonly id = this.route.snapshot.paramMap.get('id') ?? '';
  protected readonly soort: string = this.route.snapshot.data['soort'];
  protected readonly titel: string = this.route.snapshot.data['titel'];
  protected readonly profiel = signal<Profiel | null>(null);
  protected readonly teams = signal<Team[]>([]);
  protected readonly team = signal<TeamDetail | null>(null);
  protected readonly klassement = signal<Klassement | null>(null);
  protected readonly fout = signal('');
  protected readonly bezig = signal(false);

  ngOnInit(): void {
    this.laad();
  }

  private laad(): void {
    switch (this.soort) {
      case 'teams': this.lees('/teams', this.teams.set); break;
      case 'mijn-teams':
        this.lees('/mijn-teams', this.teams.set);
        this.gebruikers.profiel().subscribe({
          next: this.profiel.set,
          error: (error) => this.fout.set(foutmelding(error)),
        });
        break;
      case 'team': this.lees('/teams/' + this.id, this.team.set); break;
      case 'klassement': this.lees('/teams/' + this.id + '/scoreboard', this.klassement.set); break;
    }
  }

  private lees<T>(pad: string, resultaat: (waarde: T) => void): void {
    this.api.get<T>(pad).subscribe({ next: resultaat, error: (error) => this.fout.set(foutmelding(error)) });
  }

  protected actie(method: 'POST' | 'DELETE', pad: string, terug?: string, body?: unknown): void {
    if (this.bezig()) return;
    if (method === 'DELETE' && !window.confirm(this.taal.vertaal('Wil je dit verwijderen?'))) return;
    this.bezig.set(true);
    this.fout.set('');
    this.api.schrijf(method, pad, body).subscribe({
      next: () => {
        this.bezig.set(false);
        if (terug) void this.router.navigateByUrl(terug);
        else this.laad();
      },
      error: (error) => {
        this.bezig.set(false);
        this.fout.set(foutmelding(error));
      },
    });
  }
}
