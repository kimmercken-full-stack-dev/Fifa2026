import { HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Api } from '../shared/api';
import type { Profiel } from './profiel';

@Injectable({ providedIn: 'root' })
export class GebruikersService {
  private readonly api = inject(Api);

  profiel() {
    return this.api.get<Profiel>('/profiel');
  }

  aanmelden(username: string, password: string) {
    return this.api.schrijf<void>('POST', '/login', new HttpParams({ fromObject: { username, password } }));
  }

  afmelden() {
    return this.api.schrijf<void>('POST', '/logout');
  }
}
