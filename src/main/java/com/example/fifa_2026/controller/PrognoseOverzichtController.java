package com.example.fifa_2026.controller;

import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.PrognoseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/prognoses")
public class PrognoseOverzichtController {

    private final PrognoseService prognoseService;
    private final CurrentUserService currentUserService;

    /** Laadt eigen voorspellingen en berekent per voorspelling of de wijzigingsdeadline nog niet verstreken is. */
    @GetMapping
    public String overzicht(Model model) {
        long gebruikerId = currentUserService.getCurrentGebruikerIdOrThrow();

        var prognoses = prognoseService.getPrognosesForGebruiker(gebruikerId);
        model.addAttribute("prognoses", prognoses);

        java.util.Map<Long, Boolean> canEditPrognoseMap = new java.util.HashMap<>();
        var now = java.time.LocalDateTime.now();

        for (var p : prognoses) {
            var w = p.getWedstrijd();
            var start = java.time.LocalDateTime.of(w.getSpeeldatum(), w.getStarttijd().toLocalTime());
            var deadline = start.minusHours(1);
            canEditPrognoseMap.put(p.getId(), now.isBefore(deadline));
        }

        model.addAttribute("canEditPrognoseMap", canEditPrognoseMap);
        return "prognose-overzicht";
    }

    /** Laat de service een voorspelling van de ingelogde gebruiker verwijderen. */
    @PostMapping("/{prognoseId}/delete")
    public String delete(@PathVariable long prognoseId) {
        long gebruikerId = currentUserService.getCurrentGebruikerIdOrThrow();
        prognoseService.deletePrognoseById(prognoseId, gebruikerId);
        return "redirect:/prognoses";
    }
}