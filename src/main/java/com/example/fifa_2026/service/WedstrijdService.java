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

    /** Haalt totalen in één query op en bewaart de betrokken gebruikers samen. */
    private void herberekenScoresVoorWedstrijd(long wedstrijdId) {
    // Haal de puntwaarden uit de i18n-bestanden op.
    // De uitslag en gebruikersscores worden binnen dezelfde transactie opgeslagen.
        var scores = prognoseRepository.berekenScoresVoorWedstrijd(
                wedstrijdId, score("score.exact"), score("score.winnaar"),
                score("score.bonus.exact.uniek"), score("score.bonus.winnaar.uniek"));
        if (scores.isEmpty()) {
            // Zonder betrokken prognoses hoeven we geen gebruikers op te halen.
            return;
        }
        var totalen = scores.stream().collect(Collectors.toMap(
                PrognoseRepository.GebruikerScore::getGebruikerId,
                s -> Math.toIntExact(s.getTotaal())));
        // Haal alle betrokken gebruikers samen op, in plaats van findById in een lus.
        var gebruikers = gebruikerRepository.findAllById(totalen.keySet());
        gebruikers.forEach(g -> g.setScore(totalen.get(g.getId())));
        // Bewaar de gebruikers; Hibernate verstuurt de updates in batches van maximaal 50.
        gebruikerRepository.saveAll(gebruikers);
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
