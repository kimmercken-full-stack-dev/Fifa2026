package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.PrognoseFormDTO;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.PrognoseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/prognose")
public class PrognoseController {

    private final PrognoseService prognoseService;
    private final CurrentUserService currentUserService;

    /** Laadt de wedstrijd en de eventuele bestaande voorspelling van de ingelogde gebruiker. */
    @GetMapping("/{wedstrijdId}")
    public String showForm(@PathVariable long wedstrijdId, Model model) {

        long gebruikerId = currentUserService.getCurrentGebruikerIdOrThrow();

        var wedstrijd = prognoseService.getWedstrijdOrThrow(wedstrijdId);

        PrognoseFormDTO form =
                prognoseService.getPrognoseFormForWedstrijdAndGebruiker(
                        wedstrijdId,
                        gebruikerId
                );

        model.addAttribute("wedstrijd", wedstrijd);
        model.addAttribute("prognoseForm", form);
        model.addAttribute("eigenPrognose", form);
        model.addAttribute("magWijzigenPrognose", true);

        return "prognose-form-page";
    }

    /** Toont invoer- of deadlinefouten in het formulier en verwijst na opslaan naar het prognoseoverzicht. */
    @PostMapping
    public String submit(@ModelAttribute("prognoseForm") @Valid PrognoseFormDTO form,
                         BindingResult bindingResult,
                         Model model) {
        long gebruikerId = currentUserService.getCurrentGebruikerIdOrThrow();

        if (bindingResult.hasErrors()) {
            model.addAttribute("wedstrijd", prognoseService.getWedstrijdOrThrow(form.getWedstrijdId()));
            model.addAttribute("prognoseForm", form); // <===
            return "prognose-form-page";
        }

        try {
            prognoseService.saveOrUpdatePrognose(form, gebruikerId);
        } catch (IllegalStateException ex) {
            model.addAttribute("wedstrijd", prognoseService.getWedstrijdOrThrow(form.getWedstrijdId()));
            model.addAttribute("prognoseForm", form); // <===
            model.addAttribute("error", ex.getMessage());
            return "prognose-form-page";
        }

        return "redirect:/prognoses";
    }
}