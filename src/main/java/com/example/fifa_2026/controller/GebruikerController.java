package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.GebruikerRegistratieDTO;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.GebruikerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class GebruikerController {

    private final GebruikerService gebruikerService;
    private final CurrentUserService currentUserService;

    /** Vult de profielpagina met het account van de ingelogde gebruiker. */
    @GetMapping("/gebruiker")
    public String profiel(Model model) {
        model.addAttribute("gebruiker", currentUserService.getCurrentGebruikerOrThrow());
        return "profiel";
    }

    /** Toont een leeg registratieformulier. */
    @GetMapping("/registreer")
    public String showRegister(Model model) {
        model.addAttribute("form", new GebruikerRegistratieDTO());
        return "registreer";
    }

    /** Verwerkt geldige registratie-invoer en toont validatie- of registratieproblemen opnieuw in het formulier. */
    @PostMapping("/registreer")
    public String doRegister(@ModelAttribute("form") @Valid GebruikerRegistratieDTO form,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "registreer";
        }

        try {
            gebruikerService.registreer(form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registerError", ex.getMessage());
            return "registreer";
        }

        return "redirect:/login";
    }
}