import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, switchMap } from 'rxjs';

type CsrfToken = { headerName: string; token: string };

@Injectable({ providedIn: 'root' })
export class Api {
  private readonly http = inject(HttpClient);

  get<T>(pad: string): Observable<T> {
    return this.http.get<T>('/api' + pad);
  }

  schrijf<T>(method: 'POST' | 'PUT' | 'DELETE', pad: string, body?: unknown): Observable<T> {
    return this.get<CsrfToken>('/csrf').pipe(switchMap(({ headerName, token }) =>
      this.http.request<T>(method, '/api' + pad, { body, headers: { [headerName]: token } }),
    ));
  }
}

export function foutmelding(error: HttpErrorResponse): string {
  if (error.status === 401) return 'Meld je aan of controleer je gebruikersnaam en wachtwoord.';
  if (error.status === 403) return 'Je hebt geen toegang tot deze actie. Vernieuw de pagina als je sessie verlopen is.';
  return error.error?.fouten?.join(' ') || error.error?.detail || 'De aanvraag is mislukt. Probeer opnieuw.';
}
