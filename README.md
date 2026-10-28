# FIFA 2026 — wedstrijdprognoses en teams

Een Java-webapplicatie die ik ontwikkelde voor het vak **Enterprise and Web Development** binnen de opleiding Toegepaste Informatica aan HOGENT.

Gebruikers bekijken voetbalwedstrijden, voorspellen uitslagen en vormen teams. Beheerders beheren wedstrijden en voeren uitslagen in. Op basis daarvan worden gebruikersscores herberekend en teamrangschikkingen getoond.

**Auteur:** Kim Mercken. Een onderwijs- en portfolioproject met fictieve demogegevens, zonder echte gebruikers. De wedstrijden zijn voorbeelddata, geen officieel WK-schema.

## Functionaliteit

- Registratie en formulierlogin met gebruikers- en beheerdersrollen.
- Wedstrijdoverzicht, details en beheer van wedstrijden en uitslagen.
- Persoonlijke voorspellingen, aanpasbaar tot een uur voor de wedstrijd.
- Teams aanmaken, aansluiten met een uitnodigingscode en ledenbeheer door de eigenaar.
- Persoonlijke scores, teamklassement en een top 10 van teams.
- Nederlandse en Engelse teksten.
- Een REST-endpoint voor stadioncapaciteit, zoals gevraagd in de opleidingsopdracht.
- Een REST-endpoint om wedstrijden op speeldatum op te vragen.

## Technische opbouw

Java 21, Spring Boot 3.5.13, Spring MVC, Thymeleaf, Spring Security, Spring Data JPA/Hibernate, MySQL, Jakarta Validation en Lombok. WebClient demonstreert een HTTP-aanroep naar het eigen capaciteit-endpoint. De tests gebruiken onder meer JUnit 5, MockMvc, Mockito en AssertJ.

De applicatie is een gelaagde monoliet:

```text
Browser → MVC-controller → Service → Repository → MySQL
              ↓              ↓
         Thymeleaf         Domeinmodel

REST-client → REST-controller → Service → DTO → JSON
```

Controllers verwerken verzoeken en formulieren. Services bevatten onder meer de prognosedeadline, eigenaarscontroles en scoreberekening. Repositories verzorgen de databasebenadering. DTO's scheiden formulier- en responsgegevens van JPA-entiteiten.

### REST-endpoints

| Controller | GET-endpoint | Werking |
| --- | --- | --- |
| `LocatieRestController` | `/locaties/{id}/capaciteit` | Geeft het locatie-ID en de stadioncapaciteit terug als JSON. Dit endpoint was een vereiste van de opdracht en wordt via WebClient aangeroepen vanuit de wedstrijddetailpagina. |
| `WedstrijdRestController` | `/wedstrijden/{speeldatum}` | Geeft de wedstrijden voor een speeldatum terug als JSON. Accepteert `yyyy-MM-dd` en `dd-MM-yyyy`; een ongeldige datum geeft HTTP 400. |

Uitleg over de werking staat rechtstreeks bij de relevante klassen en methoden in de code.

## Lokaal starten

### Benodigdheden

- JDK 21.
- MySQL met een **aparte, lege demodatabase**.
- Maven 3.9.x of de meegeleverde Maven Wrapper. De eerste build heeft internet nodig.

### Database en instellingen

Maak bijvoorbeeld `fifa2026_demo` aan en gebruik een databaseaccount met rechten op uitsluitend die database. Kopieer vanuit de projectmap:

```powershell
Copy-Item config/application-local.properties.example config/application-local.properties
```

Vul in het gekopieerde bestand uw eigen databasegebruikersnaam, databasewachtwoord en een zelfgekozen `app.demo-password` in. Git negeert dit bestand. Er worden geen echte wachtwoorden meegeleverd. Spring leest geen `.env`-bestand automatisch in.

**Het voorbeeld activeert het profiel `dev`. Dat maakt de tabellen bij elke start opnieuw aan en vult fictieve demoaccounts, teams en wedstrijden in. Gebruik uitsluitend een wegwerpdatabase.**

### Startopdracht

Voer vanuit de projectmap uit:

```powershell
mvn spring-boot:run
```

Of met de wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

Op macOS/Linux: `sh mvnw spring-boot:run`.

De oorspronkelijke Windows-wrapper kan op sommige systemen `Cannot index into a null array` melden. Gebruik dan een geinstalleerde Maven met bovenstaande `mvn`-opdracht. Het oorspronkelijke script is behouden.

Open [http://localhost:8080/wedstrijd](http://localhost:8080/wedstrijd).

### Demoaccounts

| Rol | Gebruikersnaam | Wachtwoord |
| --- | --- | --- |
| Beheerder | `baardmans` | Zelf ingesteld via `app.demo-password` |
| Gebruiker | `winner` | Zelf ingesteld via `app.demo-password` |
| Gebruiker | `alex` | Zelf ingesteld via `app.demo-password` |

Alle seeds van de accounts gebruiken dat lokale demowachtwoord. Registraties krijgen rol `USER`; wachtwoorden worden met BCrypt gehasht.

## Tests

De bestaande tests staan in `src/test/java`. Spring-contexttests hebben bij de meegeleverde configuratie een bereikbare MySQL-database nodig. Gebruik een **aparte testdatabase**.

Maak `config/application-test-local.properties` met de URL, gebruikersnaam en het wachtwoord van uw testdatabase, via de sleutels `spring.datasource.url`, `spring.datasource.username` en `spring.datasource.password`. Dit bestand wordt door Git genegeerd. Voer uit:

```powershell
mvn test "-Dspring.config.import=optional:file:./config/application-test-local.properties" "-Dspring.profiles.active=test" "-Dspring.jpa.hibernate.ddl-auto=create-drop"
```

Het profiel `test` voorkomt demo-initialisatie; `create-drop` maakt en verwijdert de testtabellen. Sommige tests zijn tijdsafhankelijk: de toegestane wedstrijdperiode is vastgelegd op 5 april tot en met 5 december 2026.

## Gebruikersinterface

De gebruikersinterface is opgezet om de functionaliteit van de applicatie te demonstreren. De nadruk van deze opdracht lag op de technische werking, validatie en achterliggende logica. Aan de visuele vormgeving is beperkt aandacht besteed; dit project is daarom vooral een voorbeeld van mijn softwareontwikkeling, niet van UI-design.
