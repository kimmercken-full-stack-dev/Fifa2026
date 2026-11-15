import { DOCUMENT } from '@angular/common';
import { inject, Injectable, Pipe, PipeTransform, signal } from '@angular/core';
import { Api } from './api';

const engels: Readonly<Record<string, string>> = {
  "Positie": "Position",
  "Eigenaar": "Owner",
  "Aantal leden": "Number of members",
  "Totale score": "Total score",
  "Acties": "Actions",
  "Aansluiten": "Join team",
  "Wedstrijd": "Match",
  "Prognose": "Prediction",
  "Gelijkspel": "Draw",
  "Rol": "Role",
  "Jouw prognose": "Your prediction",
  "Er is nog geen prognose voor deze wedstrijd.": "No prediction has been submitted for this match yet.",
  "Je kunt je prognose aanpassen tot één uur voor de aftrap.": "You can still update your prediction until one hour before kick-off.",
  "De officiële eindstand is nog niet bekend.": "The official final score is not available yet.",
  "Opslaan": "Save",
  "Prognose opgeslagen.": "Prediction saved.",
  "Hoofdnavigatie": "Main navigation",
  "Wedstrijden": "Games",
  "Nieuwe wedstrijd": "New Game",
  "Prognoses": "Forecasts",
  "Top 10 teams": "Top 10 teams",
  "Mijn prognoses": "My predictions",
  "Mijn teams": "My teams",
  "Profiel": "Profile",
  "Wedstrijd aanmaken": "Create match",
  "Afmelden": "Logout",
  "Inloggen": "Sign in",
  "Registreren": "Register",
  "Wedstrijdoverzicht": "Match overview",
  "Land 1": "Country 1",
  "Land 2": "Country 2",
  "Speeldatum": "Match date",
  "Starttijd": "Kick-off",
  "Stad – Stadion": "City – Stadium",
  "Uitslag land 1 – Uitslag land 2": "Score",
  "Winnaar": "Winner",
  "Bekijk": "View",
  "Terug naar het wedstrijdoverzicht": "Back to matches",
  "om": "at",
  "Stadioncapaciteit": "Stadium capacity",
  "Wedstrijd voorspellen": "Predict match",
  "De deadline voor voorspellingen is verstreken.": "The prediction deadline has passed.",
  "Wedstrijd wijzigen": "Edit match",
  "Uitslag invoeren": "Enter result",
  "Verwijder wedstrijd": "Delete match",
  "Meld je aan om een prognose te maken": "Sign in to make a prediction",
  "Uitslag": "Score",
  "Resultaat": "Result",
  "Kies een stadion": "Choose a stadium",
  "Bevestigen": "Confirm",
  "Terug": "Back",
  "Score": "Score",
  "Team aanmaken": "Create team",
  "Aansluiten met code": "Join with code",
  "Klassement": "Standings",
  "punten": "points",
  "leden": "members",
  "Uitnodigingscode": "Invite code",
  "Vernieuw code": "Regenerate code",
  "Verwijder team": "Delete team",
  "Verwijder lid": "Remove member",
  "Wijzigen": "Edit",
  "Verwijderen": "Delete",
  "Doelpunten land 1": "Goals for country 1",
  "Doelpunten land 2": "Goals for country 2",
  "Stadion": "Stadium",
  "Aanmelden": "Sign in",
  "Gebruikersnaam": "Username",
  "Wachtwoord": "Password",
  "Voornaam": "First name",
  "Familienaam": "Last name",
  "Herhaal wachtwoord": "Repeat password",
  "Teamnaam": "Team name",
  "Aansluiten bij een team": "Join a team",
  "Teamklassement": "Team standings",
  "Teampagina": "Team page",
  "Teamoverzicht": "Team overview",
  "Pagina niet gevonden": "Page not found",
  "Naar het wedstrijdoverzicht": "Go to matches",
  "Wedstrijden ophalen lukt niet.": "Could not load matches.",
  "Wedstrijd ophalen…": "Loading match…",
  "Wedstrijd ophalen lukt niet.": "Could not load the match.",
  "Wil je deze wedstrijd verwijderen?": "Delete this match?",
  "Wil je dit verwijderen?": "Delete this item?",
  "Meld je aan of controleer je gebruikersnaam en wachtwoord.": "Sign in or check your username and password.",
  "Je hebt geen toegang tot deze actie. Vernieuw de pagina als je sessie verlopen is.": "You do not have access to this action. Refresh the page if your session has expired.",
  "De aanvraag is mislukt. Probeer opnieuw.": "The request failed. Try again.",
  "Taal": "Language",
  "De taalinstelling kon niet worden opgeslagen op de server.": "The language preference could not be saved on the server."
};

@Injectable({ providedIn: 'root' })
export class TaalService {
  private readonly document = inject(DOCUMENT);
  private readonly api = inject(Api);
  private readonly taal = signal(this.document.defaultView?.localStorage.getItem('fifa-taal') === 'en' ? 'en' : 'nl');
  readonly huidig = this.taal.asReadonly();
  readonly fout = signal('');

  constructor() {
    this.document.documentElement.lang = this.huidig();
  }

  wijzig(taal: string): void {
    if (taal !== 'nl' && taal !== 'en') return;
    this.taal.set(taal);
    this.document.documentElement.lang = taal;
    this.document.defaultView?.localStorage.setItem('fifa-taal', taal);
    this.fout.set('');
    this.api.schrijf('PUT', '/taal', { taal }).subscribe({
      error: () => this.fout.set('De taalinstelling kon niet worden opgeslagen op de server.'),
    });
  }

  vertaal(tekst: string): string {
    return this.huidig() === 'en' ? engels[tekst] ?? tekst : tekst;
  }
}

@Pipe({ name: 'vertaal', pure: false })
export class VertaalPipe implements PipeTransform {
  private readonly taal = inject(TaalService);

  transform(tekst: string): string {
    return this.taal.vertaal(tekst);
  }
}
