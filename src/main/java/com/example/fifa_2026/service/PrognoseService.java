package com.example.fifa_2026.service;

import com.example.fifa_2026.dto.PrognoseFormDTO;
import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Prognose;
import com.example.fifa_2026.model.Wedstrijd;
import com.example.fifa_2026.repository.GebruikerRepository;
import com.example.fifa_2026.repository.PrognoseRepository;
import com.example.fifa_2026.repository.WedstrijdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Beheert voorspellingen per gebruiker en wedstrijd.
 * De deadline wordt hier afgedwongen, zodat ook een rechtstreeks POST-verzoek
 * de tijdsbeperking van het formulier niet kan omzeilen.
 */
@Service
@RequiredArgsConstructor
public class PrognoseService {

    private final PrognoseRepository prognoseRepository;
    private final WedstrijdRepository wedstrijdRepository;
    private final GebruikerRepository gebruikerRepository;
    private final MessageSource messageSource;

    // --- voor form ---
    public Wedstrijd getWedstrijdOrThrow(Long wedstrijdId) {

        return wedstrijdRepository.findById(wedstrijdId)
                .orElseThrow(() -> new IllegalArgumentException(msg("wedstrijd.notfound", wedstrijdId)));
    }

    /** Controleert de deadline, zoekt de bestaande voorspelling en bewaart de scores en afgeleide winnaar. */
    @Transactional
    public Prognose saveOrUpdatePrognose(PrognoseFormDTO form, long gebruikerId) {

        Wedstrijd wedstrijd = getWedstrijdOrThrow(form.getWedstrijdId());

        Gebruiker gebruiker = getGebruikerOrThrow(gebruikerId);

        // tot max 1 uur vóór start aanpassen
        LocalDateTime start = LocalDateTime.of(
                wedstrijd.getSpeeldatum(),
                wedstrijd.getStarttijd().toLocalTime());

        LocalDateTime deadline = start.minusHours(1);

        LocalDateTime now = LocalDateTime.now();

        if (!now.isBefore(deadline)) {

            throw new IllegalStateException(msg("prognose.deadline", deadline));
        }

        Prognose p = prognoseRepository
                .findByGebruiker_IdAndWedstrijd_WedstrijdID(gebruikerId, wedstrijd.getWedstrijdID())
                .orElseGet(Prognose::new);

        p.setWedstrijd(wedstrijd);
        p.setGebruiker(gebruiker);
        p.setUitslagLand1(form.getUitslagLand1());
        p.setUitslagLand2(form.getUitslagLand2());
        // winnaar automatisch bepalen
        String winnaar;
        if (form.getUitslagLand1() > form.getUitslagLand2()) {
            winnaar = wedstrijd.getLand1();
        } else if (form.getUitslagLand2() > form.getUitslagLand1()) {
            winnaar = wedstrijd.getLand2();
        } else {
            winnaar = "GELIJKSPEL";
        }

        p.setWinnaar(winnaar);
        return prognoseRepository.save(p);
    }

    // formulier ophalen
    /** Maakt een formulier voor deze wedstrijd en vult het met een eventuele bestaande voorspelling. */
    public PrognoseFormDTO getPrognoseFormForWedstrijdAndGebruiker(long wedstrijdId, long gebruikerId) {

        getWedstrijdOrThrow(wedstrijdId);
        PrognoseFormDTO form = new PrognoseFormDTO();
        form.setWedstrijdId(wedstrijdId);
        var existing = findByGebruikerAndWedstrijd(gebruikerId, wedstrijdId);

        existing.ifPresent(p -> {
            form.setUitslagLand1(p.getUitslagLand1());
            form.setUitslagLand2(p.getUitslagLand2());});

        return form;
    }

    public Wedstrijd getWedstrijdForForm(PrognoseFormDTO form) {

        return getWedstrijdOrThrow(form.getWedstrijdId());
    }

    // --- overzicht ---
    public List<Prognose> getPrognosesForGebruiker(long gebruikerId) {

        return prognoseRepository.findAllByGebruiker_IdOrderByWedstrijd_SpeeldatumAscWedstrijd_StarttijdAsc(gebruikerId);
    }

    /** Indexeert de voorspellingen op wedstrijd-ID voor gebruik in het wedstrijdoverzicht. */
    public Map<Long, Prognose> getPrognoseMapForGebruiker(long gebruikerId) {

        return prognoseRepository.findAllByGebruiker_Id(gebruikerId)
                .stream().collect(Collectors.toMap(
                        p -> p.getWedstrijd().getWedstrijdID(),
                        p -> p));
    }

    public java.util.Optional<Prognose> findByGebruikerAndWedstrijd(long gebruikerId, long wedstrijdId) {

        return prognoseRepository
                .findByGebruiker_IdAndWedstrijd_WedstrijdID(
                        gebruikerId,
                        wedstrijdId);
    }

    /** Verwijdert alleen een voorspelling die bij de opgegeven gebruiker hoort. */
    @Transactional
    public void deletePrognoseById(long prognoseId, long gebruikerId) {

        prognoseRepository.deleteByIdAndGebruiker_Id(
                prognoseId,
                gebruikerId);
    }

    public Gebruiker getGebruikerOrThrow(long gebruikerId) {

        return gebruikerRepository.findById(gebruikerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        msg("user.notfound", gebruikerId)));
    }

    private String msg(String key, Object... args) {

        return messageSource.getMessage(key, args, Locale.getDefault());
    }
}
