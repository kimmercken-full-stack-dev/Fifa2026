# FIFA 2026 — Angular-frontend

De Angular-frontend van FIFA 2026 gebruikt Angular 22, TypeScript en de REST-API van de Spring Boot-backend. De interface is beschikbaar in het Nederlands en het Engels.

## Wat je in de app kunt doen

- Bekijk het openbare wedstrijdoverzicht op `/` en de top 10 van teams op `/teams`.
- Meld je aan om wedstrijddetails, je profiel, je teams en je prognoses te bekijken. Je kunt een prognose opslaan of wijzigen tot één uur voor de aftrap.
- Maak een team aan of sluit je aan met een uitnodigingscode. Als teameigenaar kun je leden beheren en de uitnodigingscode vernieuwen.
- Beheerders kunnen wedstrijden aanmaken, wijzigen en verwijderen en uitslagen invoeren.

## Lokaal starten

Gebruik Node.js 22 vanaf versie 22.22.3, met npm. Start eerst de Spring Boot-backend op poort 8080 volgens de [README van de repository](../README.md#lokaal-starten). Voer daarna vanuit de map `angular_frontend` uit:

```powershell
npm.cmd ci
npm.cmd start
```

Open de [Angular-app](http://localhost:4200/). Je kunt je aanmelden met de [demoaccounts uit de algemene README](../README.md#demoaccounts) als de backend met het profiel `dev` draait.

Gebruik op macOS of Linux `npm` in plaats van `npm.cmd`.

## API en projectstructuur

Tijdens het ontwikkelen stuurt [de proxyconfiguratie](config/proxy.conf.json) verzoeken naar `/api` door naar Spring Boot. Het overzicht haalt wedstrijden op via `GET /api/wedstrijden`. Spring Security beheert de aanmelding met een sessiecookie. Voor schrijfacties haalt de gedeelde API-service een CSRF-token op via `GET /api/csrf` en stuurt het mee. De backend controleert wie welke handelingen mag uitvoeren.

| Map | Inhoud |
| --- | --- |
| `src/app/wedstrijden`, `gebruikers`, `teams`, `prognoses` | Pagina's, routes en logica per onderdeel |
| `src/app/layout` | Hoofdcomponent en navigatie |
| `src/app/config` | Angular-configuratie en centrale routes |
| `src/app/shared` en `src/app/formulieren` | Gedeelde API, vertalingen, foutpagina en herbruikbaar formulier |
| `src/styles` | Globale stijlen en het Angular Material-thema |

## Productiebuild

Voer uit:

```powershell
npm.cmd run build
```

De productiebuild komt in `dist/`. Bij hosting van deze build moet de webserver aanvragen naar `/api` doorsturen naar Spring Boot. De proxyconfiguratie hierboven wordt gebruikt door de lokale ontwikkelserver.

## Tests

Voer de unittests uit met [Vitest](https://vitest.dev/):

```powershell
npm.cmd test -- --watch=false
```

De tests controleren onder meer het startscherm en hoe het aanmeldformulier de gebruikersnaam en het wachtwoord naar de API verstuurt.
