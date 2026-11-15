import { VertaalPipe } from '../shared/taal';
import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { WedstrijdService } from './wedstrijd.service';
import type { Wedstrijd } from './model';
@Component({
  selector: 'app-wedstrijdoverzicht',
  templateUrl: './wedstrijdoverzicht.html',
  imports: [DatePipe, RouterLink, VertaalPipe],
})
export class Wedstrijdoverzicht implements OnInit {
  private readonly wedstrijdService = inject(WedstrijdService);

  protected readonly wedstrijden = signal<Wedstrijd[]>([]);
  protected readonly fout = signal('');

  ngOnInit(): void {
    this.wedstrijdService.getAlle().subscribe({
      next: (wedstrijden) => this.wedstrijden.set(wedstrijden),
      error: () => this.fout.set('Wedstrijden ophalen lukt niet.'),
    });
  }
}
