import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Api } from '../shared/api';
import type { Wedstrijd } from './model';

@Injectable({ providedIn: 'root' })
export class WedstrijdService {
  private readonly api = inject(Api);

  getAlle(): Observable<Wedstrijd[]> {
    return this.api.get<Wedstrijd[]>('/wedstrijden');
  }
}
