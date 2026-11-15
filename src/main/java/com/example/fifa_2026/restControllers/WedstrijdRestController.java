package com.example.fifa_2026.restControllers;

import com.example.fifa_2026.dto.WedstrijdDTO;
import com.example.fifa_2026.dto.WedstrijdCreateDTO;
import com.example.fifa_2026.dto.WedstrijdEditDTO;
import com.example.fifa_2026.dto.WedstrijdUitslagDTO;
import com.example.fifa_2026.service.PrognoseService;
import com.example.fifa_2026.service.WedstrijdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class WedstrijdRestController {
    private final WedstrijdService wedstrijdService;

    record WedstrijdDetail(long wedstrijdID, String land1, String land2, String speeldatum,
                           String starttijd, String stadion, String stad, int capaciteit,
                           Integer uitslagLand1, Integer uitslagLand2, String winnaar,
                           boolean magVoorspellen, boolean magUitslagInvullen) {}
    record WedstrijdFormulier(String land1, String land2, String speeldatum, String starttijd,
                              long locatieId, Integer uitslagLand1, Integer uitslagLand2) {}

    /** Geeft wedstrijd-DTO's als JSON terug voor de opgegeven speeldatum. */
    @GetMapping("/wedstrijden/{speeldatum}")
    public List<WedstrijdDTO> getBySpeeldatum(@PathVariable String speeldatum) {
        return wedstrijdService.getWedstrijdenVoorDatum(parseSpeeldatum(speeldatum));
    }

    @GetMapping("/wedstrijden")
    public List<WedstrijdDTO> getAll() {
        return wedstrijdService.getAllWedstrijden();
    }

    @GetMapping("/api/wedstrijden")
    public List<WedstrijdDTO> overzicht(@RequestParam(name = "speeldatum", required = false) String speeldatum) {
        return speeldatum == null ? getAll() : getBySpeeldatum(speeldatum);
    }

    @GetMapping("/api/beheer/wedstrijden/{id}")
    public WedstrijdFormulier wedstrijd(@PathVariable long id) {
        var w = wedstrijdService.getWedstrijdEntityOrThrow(id);
        return new WedstrijdFormulier(w.getLand1(), w.getLand2(), w.getSpeeldatum().toString(),
                w.getStarttijd().toLocalTime().toString(), w.getLocatie().getId(),
                w.getUitslagLand1(), w.getUitslagLand2());
    }

    @PostMapping("/api/beheer/wedstrijden")
    public long maakWedstrijd(@Valid @RequestBody WedstrijdCreateDTO form) {
        return wedstrijdService.createWedstrijd(form).getWedstrijdID();
    }

    @PutMapping("/api/beheer/wedstrijden/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void wijzigWedstrijd(@PathVariable long id, @Valid @RequestBody WedstrijdEditDTO form) {
        wedstrijdService.editWedstrijd(id, form);
    }

    @PutMapping("/api/beheer/wedstrijden/{id}/uitslag")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uitslag(@PathVariable long id, @Valid @RequestBody WedstrijdUitslagDTO form) {
        wedstrijdService.updateUitslag(id, form);
    }

    @DeleteMapping("/api/beheer/wedstrijden/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verwijderWedstrijd(@PathVariable long id) {
        wedstrijdService.deleteWedstrijd(id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/wedstrijden/{id}")
    public WedstrijdDetail wedstrijdDetail(@PathVariable long id) {
        var w = wedstrijdService.getWedstrijdEntityOrThrow(id);
        var plaats = w.getLocatie();
        var aftrap = w.getSpeeldatum().atTime(w.getStarttijd().toLocalTime());
        var nu = LocalDateTime.now();
        return new WedstrijdDetail(id, w.getLand1(), w.getLand2(), w.getSpeeldatum().toString(),
                w.getStarttijd().toString(), plaats.getStadion(), plaats.getStad(), plaats.getCapaciteit(),
                w.getUitslagLand1(), w.getUitslagLand2(), w.getWinnaar(),
                nu.isBefore(PrognoseService.prognoseDeadline(aftrap)), !nu.isBefore(WedstrijdService.uitslagVanaf(aftrap)));
    }

    /** Accepteert twee datumformaten en geeft HTTP 400 wanneer beide omzettingen mislukken. */
    private LocalDate parseSpeeldatum(String speeldatum) {
        try {
            return LocalDate.parse(speeldatum);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(speeldatum, DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            } catch (DateTimeParseException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Gebruik datumformaat yyyy-MM-dd of dd-MM-yyyy");
            }
        }
    }
}
