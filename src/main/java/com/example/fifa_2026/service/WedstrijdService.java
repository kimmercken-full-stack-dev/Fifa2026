package com.example.fifa_2026.service;

import com.example.fifa_2026.dto.WedstrijdCreateDTO;
import com.example.fifa_2026.dto.WedstrijdDTO;
import com.example.fifa_2026.dto.WedstrijdEditDTO;
import com.example.fifa_2026.dto.WedstrijdUitslagDTO;
import com.example.fifa_2026.exception.wedstrijdmomentNietBeschikbaarInDitStadionException;
import com.example.fifa_2026.model.*;
import com.example.fifa_2026.repository.GebruikerRepository;
import com.example.fifa_2026.repository.LocatieRepository;
import com.example.fifa_2026.repository.PrognoseRepository;
import com.example.fifa_2026.repository.WedstrijdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Beheert wedstrijden, stadionplanning en uitslagen.
 * Een uitslagwijziging en de herberekening van gebruikersscores gebeuren binnen
 * dezelfde transactie, zodat ze samen worden opgeslagen of teruggedraaid.
 */
@Service
@RequiredArgsConstructor
public class WedstrijdService {

    private final WedstrijdRepository repo;
    private final LocatieRepository locatieRepository;
    private final PrognoseRepository prognoseRepository;
    private final GebruikerRepository gebruikerRepository;
    private final MessageSource messageSource;

    private List<WedstrijdDTO> convertToDTOList(List<Wedstrijd> wedstrijden) {
        return wedstrijden.stream()
                .map(w -> new WedstrijdDTO(
                        w.getWedstrijdID(),
                        w.getLand1(),
                        w.getLand2(),
                        w.getSpeeldatum(),
                        w.getStarttijd(),
                        w.getUitslagLand1(),
                        w.getUitslagLand2(),
                        w.getWinnaar(),
                        w.getLocatie().getStad(),
                        w.getLocatie().getStadion(),
                        w.getLocatie().getStadiumcode(),
                        w.getLocatie().getChecksum())).toList();
    }

    public List<WedstrijdDTO> getAllWedstrijden() {
        return convertToDTOList(repo.findAllByOrderBySpeeldatumAscStarttijdAsc());
    }

    public List<Locatie> getAllLocaties() {
        return locatieRepository.findAll();
    }

    /** Controleert de locatie en overlap met wedstrijden in hetzelfde stadion en bewaart de nieuwe wedstrijd. */
    @Transactional
    public Wedstrijd createWedstrijd(WedstrijdCreateDTO form) {

        Locatie locatie = locatieRepository.findById(form.getLocatieId())
                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage(
                                "error.locatie.nietgevonden",
                                new Object[]{form.getLocatieId()},
                                Locale.getDefault())));

        LocalDate datum = LocalDate.parse(form.getSpeeldatum());
        LocalTime nieuweStart = LocalTime.parse(form.getStarttijd());
        LocalTime nieuweEind = nieuweStart.plusHours(2);
        List<Wedstrijd> bestaande = repo.findBySpeeldatumAndLocatie(datum, locatie);

        for (Wedstrijd bestaand : bestaande) {
            LocalTime bestaandStart = bestaand.getStarttijd().toLocalTime();
            LocalTime bestaandEind = bestaandStart.plusHours(2);
            boolean overlapt = !nieuweEind.isBefore(bestaandStart)
                            && !nieuweStart.isAfter(bestaandEind.minusSeconds(1));

            if (overlapt) {
                throw new wedstrijdmomentNietBeschikbaarInDitStadionException();
            }
        }

        Wedstrijd w = new Wedstrijd();
        w.setLand1(form.getLand1().trim());
        w.setLand2(form.getLand2().trim());
        w.setSpeeldatum(datum);
        w.setStarttijd(java.sql.Time.valueOf(nieuweStart));
        w.setUitslagLand1(null);
        w.setUitslagLand2(null);
        w.setWinnaar(null);
        w.setLocatie(locatie);
        return repo.save(w);
    }

    /** Verwijdert eerst de gekoppelde prognoses en daarna de wedstrijd binnen dezelfde transactie. */
    @Transactional
    public void deleteWedstrijd(long wedstrijdId) {
        Wedstrijd wedstrijd = getWedstrijdEntityOrThrow(wedstrijdId);
        // eerst gekoppelde prognoses verwijderen
        prognoseRepository.deleteAllByWedstrijd_WedstrijdID(wedstrijdId);
        // daarna wedstrijd verwijderen
        repo.delete(wedstrijd);
    }

    public Wedstrijd getWedstrijdEntityOrThrow(long wedstrijdId) {

        return repo.findById(wedstrijdId).orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("error.wedstrijd.nietgevonden",
                                new Object[]{wedstrijdId}, Locale.getDefault())));
    }

    public List<WedstrijdDTO> getWedstrijdenVoorDatum(LocalDate datum) {
        return convertToDTOList(repo.findBySpeeldatumOrderByStarttijdAsc(datum));
    }

    public Integer getCapaciteitVoorStadion(Long id) {

        return locatieRepository.findById(id)
                .map(Locatie::getCapaciteit)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("error.stadion.nietgevonden",
                                new Object[]{id}, Locale.getDefault())));
    }

    /** Weigert een te vroege uitslag, bepaalt de winnaar en herberekent scores van betrokken gebruikers. */
    @Transactional
    public void updateUitslag(long wedstrijdId, WedstrijdUitslagDTO form) {

        Wedstrijd w = getWedstrijdEntityOrThrow(wedstrijdId);
        LocalDateTime start = LocalDateTime.of(w.getSpeeldatum(), w.getStarttijd().toLocalTime());
        LocalDateTime earliestAllowed = start.plusHours(2);
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(earliestAllowed)) {
            throw new IllegalStateException(
                    messageSource.getMessage("error.uitslag.tevroeg",
                            new Object[]{earliestAllowed}, Locale.getDefault()));
        }

        int s1 = form.getUitslagLand1();
        int s2 = form.getUitslagLand2();
        w.setUitslagLand1(s1);
        w.setUitslagLand2(s2);
        w.setWinnaar(winnaarVan(w.getLand1(), w.getLand2(), s1, s2));
        repo.save(w);
        herberekenScoresVoorWedstrijd(wedstrijdId);
    }

    /** Werkt landen, datum, tijd en locatie bij; de uitgecommentarieerde overlapcontrole wordt niet uitgevoerd. */
    @Transactional
    public void editWedstrijd(long wedstrijdId, WedstrijdEditDTO form) {

        Wedstrijd w = getWedstrijdEntityOrThrow(wedstrijdId);
        LocalDateTime now = LocalDateTime.now();
        String s1 = form.getLand1();
        String s2 = form.getLand2();
        String s3 = form.getSpeeldatum();
        String s4 = form.getStarttijd();
        Long l1 = form.getLocatieId();
        Locatie locatie = locatieRepository.findById(l1)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("error.locatie.nietgevonden",
                                new Object[]{l1}, Locale.getDefault())));

        /*    -->>> NA DEADLINE TOEGEVOEGD --->>> in de code in commentaar wordt de juiste validatie nu ook doorgevoerd
        bij het aanpassen van een wedstrijd --->>> Geen 2 wedstrijden in hetzelfde stadion op hetzelfde uur (+2).
        * Long l1 = form.getLocatieId();
        Locatie locatie = locatieRepository.findById(l1)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("error.locatie.nietgevonden",
                                new Object[]{l1}, Locale.getDefault())));
        LocalDate datum = LocalDate.parse(form.getSpeeldatum());
        LocalTime s4 = LocalTime.parse(form.getStarttijd());
        LocalTime nieuweEind = s4.plusHours(2);
        List<Wedstrijd> bestaande = repo.findBySpeeldatumAndLocatie(datum, locatie);

        for (Wedstrijd bestaand : bestaande) {
            LocalTime bestaandStart = bestaand.getStarttijd().toLocalTime();
            LocalTime bestaandEind = bestaandStart.plusHours(2);
            boolean overlapt = !nieuweEind.isBefore(bestaandStart)
                    && !s4.isAfter(bestaandEind.minusSeconds(1));

            if (overlapt) {
                throw new wedstrijdmomentNietBeschikbaarInDitStadionException();
            }
        }
        * */

        w.setLand1(s1);
        w.setLand2(s2);
        w.setSpeeldatum(LocalDate.parse(s3));
        w.setStarttijd(Time.valueOf(s4));
        w.setLocatie(locatie);
        repo.save(w);

    }

    /** Zoekt betrokken gebruikers en berekent hun totale score opnieuw uit al hun gespeelde voorspellingen. */
    private void herberekenScoresVoorWedstrijd(long wedstrijdId) {

        List<Prognose> prognosesVoorWedstrijd = prognoseRepository.findAllByWedstrijd_WedstrijdID(
                        wedstrijdId);

        Set<Long> gebruikerIds = prognosesVoorWedstrijd.stream()
                        .map(p -> p.getGebruiker().getId())
                        .collect(Collectors.toSet());

        for (Long gebruikerId : gebruikerIds) {

            int totaal = herberekenScoreVoorGebruiker(gebruikerId);

            Gebruiker g = gebruikerRepository.findById(gebruikerId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    messageSource.getMessage("error.gebruiker.nietgevonden",
                                            new Object[]{gebruikerId}, Locale.getDefault())));

            g.setScore(totaal);
            gebruikerRepository.save(g);
        }
    }

    /** Telt punten op voor prognoses waarvan de wedstrijd beide definitieve scores heeft. */
    private int herberekenScoreVoorGebruiker(long gebruikerId) {

        List<Prognose> prognoses = prognoseRepository.findAllByGebruiker_Id(gebruikerId);
        int totaal = 0;
        for (Prognose p : prognoses) {
            Wedstrijd w = p.getWedstrijd();

            if (w == null) {
                continue;
            }
            if (w.getUitslagLand1() == null || w.getUitslagLand2() == null) {
                continue;
            }
            totaal += berekenPunten(p, w);
        }
        return totaal;
    }

    /**
     * Telt basispunten en bonussen per team op, met maximaal 12 punten per prognose.
     * De puntwaarden staan momenteel in de i18n-bundels onder de sleutels score.*.
     */
    private int berekenPunten(Prognose p, Wedstrijd w) {

        if (p.getUitslagLand1() == null || p.getUitslagLand2() == null) {
            return 0;
        }

        int punten = 0;

        boolean exact = p.getUitslagLand1().equals(w.getUitslagLand1())
                        && p.getUitslagLand2().equals(w.getUitslagLand2());

        String echteWinnaar = winnaarVan(
                        w.getLand1(),
                        w.getLand2(),
                        w.getUitslagLand1(),
                        w.getUitslagLand2());

        String voorspeldeWinnaar = winnaarVan(
                        w.getLand1(),
                        w.getLand2(),
                        p.getUitslagLand1(),
                        p.getUitslagLand2());

        boolean winnaarCorrect = echteWinnaar.equals(
                        voorspeldeWinnaar);

        if (exact) {
            punten += score("score.exact");
        }

        if (winnaarCorrect && !exact) {
            punten += score("score.winnaar");
        }

        for (var team : p.getGebruiker().getTeams()) {
            if (exact && heeftEnigeExacteScoreBinnenTeam(team, w, p)) {
                punten += score("score.bonus.exact.uniek");
            }

            if (!exact && winnaarCorrect && heeftEnigeJuisteWinnaarBinnenTeam(team, w, p, echteWinnaar)) {
                punten += score("score.bonus.winnaar.uniek");
            }
        }
        return Math.min(punten, 12);
    }

    /** Telt correcte exacte voorspellingen van teamleden en controleert of er precies een is. */
    private boolean heeftEnigeExacteScoreBinnenTeam(Team team, Wedstrijd wedstrijd, Prognose dezePrognose) {

        List<Prognose> voorspellingen = prognoseRepository.findAllByWedstrijd_WedstrijdID(wedstrijd.getWedstrijdID());

        long aantal = voorspellingen.stream()
                        .filter(p -> p.getGebruiker().getTeams().contains(team))
                        .filter(p -> p.getUitslagLand1() != null && p.getUitslagLand2() != null)
                        .filter(p -> p.getUitslagLand1().equals(wedstrijd.getUitslagLand1())
                                && p.getUitslagLand2().equals(wedstrijd.getUitslagLand2())).count();

        return aantal == 1;
    }

    /** Controleert of precies een teamlid de juiste winnaar of het gelijkspel voorspelde. */
    private boolean heeftEnigeJuisteWinnaarBinnenTeam(
            Team team,
            Wedstrijd wedstrijd,
            Prognose dezePrognose,
            String echteWinnaar
    ) {

        List<Prognose> voorspellingen = prognoseRepository.findAllByWedstrijd_WedstrijdID(wedstrijd.getWedstrijdID());

        long aantal = voorspellingen.stream()
                        .filter(p -> p.getGebruiker().getTeams().contains(team))
                        .filter(p -> p.getUitslagLand1() != null && p.getUitslagLand2() != null)
                        .filter(p -> {String winnaar = winnaarVan(
                                            wedstrijd.getLand1(),
                                            wedstrijd.getLand2(),
                                            p.getUitslagLand1(),
                                            p.getUitslagLand2());

                            return winnaar.equals(echteWinnaar);
                        }).count();

        return aantal == 1;
    }

    private int score(String key) {

        return Integer.parseInt(messageSource.getMessage(key, null, Locale.getDefault()));
    }

    private String winnaarVan(String land1, String land2, int s1, int s2) {

        if (s1 > s2) {
            return land1;
        }

        if (s2 > s1) {
            return land2;
        }

        return messageSource.getMessage("wedstrijd.gelijkspel", null, Locale.getDefault());
    }
}
