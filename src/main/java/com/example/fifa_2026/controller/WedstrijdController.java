package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.PrognoseFormDTO;
import com.example.fifa_2026.dto.WedstrijdCreateDTO;
import com.example.fifa_2026.dto.WedstrijdEditDTO;
import com.example.fifa_2026.dto.WedstrijdUitslagDTO;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.LocatieService;
import com.example.fifa_2026.service.PrognoseService;
import com.example.fifa_2026.service.WedstrijdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wedstrijd")
public class WedstrijdController {

    private final WedstrijdService wedstrijdService;
    private final PrognoseService prognoseService;
    private final CurrentUserService currentUserService;
    private final LocatieService locatieService;

    // 1. Overzicht (voor iedereen)
    /** Bouwt het openbare overzicht met tijdsafhankelijke acties en, indien ingelogd, eigen prognoses. */
    @GetMapping
    public String listGuest(Model model) {
        var wedstrijden = wedstrijdService.getAllWedstrijden();
        model.addAttribute("allWedstrijden", wedstrijden);
        Map<Long, Boolean> canEditMap = new java.util.HashMap<>();
        Map<Long, Boolean> canCreatePrognoseMap = new java.util.HashMap<>();
        var now = LocalDateTime.now();
        for (var w : wedstrijden) {
            var start = LocalDateTime.of(w.speeldatum(), w.starttijd().toLocalTime());
            canEditMap.put(w.wedstrijdID(), !now.isBefore(start.plusHours(2)));
            canCreatePrognoseMap.put(w.wedstrijdID(), now.isBefore(start));
        }
        model.addAttribute("canEditMap", canEditMap);
        model.addAttribute("canCreatePrognoseMap", canCreatePrognoseMap);
        Map<Long, ?> prognoseMap = Collections.emptyMap();
        try {
            long gebruikerId = currentUserService.getCurrentGebruikerIdOrThrow();
            prognoseMap = prognoseService.getPrognoseMapForGebruiker(gebruikerId);
        } catch (IllegalStateException ex) {
            // niet ingelogd -> leeg laten
        }
        model.addAttribute("prognoseMap", prognoseMap);
        return "wedstrijdoverzicht";
    }

    // 2. Detailweergave (alleen ingelogd)
    /** Vult de detailpagina met prognosegegevens, deadlinevlaggen en stadioncapaciteit. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{wedstrijdId}")
    public String wedstrijdDetail(@PathVariable long wedstrijdId, Model model) {

        var wedstrijd = wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId);
        model.addAttribute("wedstrijd", wedstrijd);
        long gebruikerId = currentUserService.getCurrentGebruikerIdOrThrow();

        PrognoseFormDTO eigenPrognose = prognoseService
                .getPrognoseFormForWedstrijdAndGebruiker(wedstrijdId, gebruikerId);

        PrognoseFormDTO prognoseForm = new PrognoseFormDTO();
        prognoseForm.setWedstrijdId(wedstrijdId);

        if (eigenPrognose != null) {
            prognoseForm.setUitslagLand1(eigenPrognose.getUitslagLand1());
            prognoseForm.setUitslagLand2(eigenPrognose.getUitslagLand2());
        }
        LocalDateTime aftrap = LocalDateTime.of(
                        wedstrijd.getSpeeldatum(),
                        wedstrijd.getStarttijd().toLocalTime());
        boolean magWijzigenPrognose = LocalDateTime.now().isBefore(aftrap.minusHours(1));
        boolean magUitslagInvullen = LocalDateTime.now().isAfter(aftrap.plusHours(2));
        Integer stadionCapaciteit = getStadionCapaciteit(wedstrijd);
        model.addAttribute("eigenPrognose", eigenPrognose);
        model.addAttribute("prognoseForm", prognoseForm);
        model.addAttribute("magWijzigenPrognose", magWijzigenPrognose);
        model.addAttribute("magUitslagInvullen", magUitslagInvullen);
        model.addAttribute("stadionCapaciteit", stadionCapaciteit);
        return "wedstrijd-detail";
    }

    /** Vraagt capaciteit via REST op en gebruikt bij een fout de reeds geladen locatie als terugval. */
    private Integer getStadionCapaciteit(com.example.fifa_2026.model.Wedstrijd wedstrijd) {
        if (wedstrijd.getLocatie() == null || wedstrijd.getLocatie().getId() == null) {
            return null;
        }

        try {
            return locatieService.getCapaciteitViaRestApi(wedstrijd.getLocatie().getId());
        } catch (RuntimeException ex) {
            return wedstrijd.getLocatie().getCapaciteit();
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/nieuw")
    public String createForm(Model model) {
        model.addAttribute("form", new WedstrijdCreateDTO());
        model.addAttribute("locaties", wedstrijdService.getAllLocaties());
        return "wedstrijd-nieuw";
    }
    /** Toont invoerfouten opnieuw of laat de service een wedstrijd aanmaken. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/nieuw")
    public String create(@ModelAttribute("form") @Valid WedstrijdCreateDTO form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("locaties", wedstrijdService.getAllLocaties());
            return "wedstrijd-nieuw";
        }
        wedstrijdService.createWedstrijd(form);
        redirectAttributes.addFlashAttribute("successMatchCreated", true);
        return "redirect:/wedstrijd";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{wedstrijdId}/delete")
    public String deleteWedstrijd(@PathVariable long wedstrijdId,
                                  RedirectAttributes redirectAttributes) {
        wedstrijdService.deleteWedstrijd(wedstrijdId);
        redirectAttributes.addFlashAttribute("successMatchDeleted", true);
        return "redirect:/wedstrijd";
    }

    // 4. Bewerken (alleen admin)
    /** Kopieert bestaande wedstrijdgegevens naar het bewerkingsformulier. */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{wedstrijdId}/edit")
    public String editWedstrijdForm(@PathVariable long wedstrijdId, Model model) {
        model.addAttribute("locaties", wedstrijdService.getAllLocaties());
        var wedstrijd = wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId);
        WedstrijdEditDTO form = new WedstrijdEditDTO();
        form.setLand1(wedstrijd.getLand1());
        form.setLand2(wedstrijd.getLand2());
        form.setSpeeldatum(String.valueOf(wedstrijd.getSpeeldatum()));
        form.setStarttijd(String.valueOf(wedstrijd.getStarttijd()));
        form.setLocatieId(wedstrijd.getLocatie().getId());
        model.addAttribute("wedstrijd", wedstrijd);
        model.addAttribute("form", form);
        return "wedstrijd-edit";
    }
    /** Toont validatiefouten of bewaart de gewijzigde wedstrijd via de service. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{wedstrijdId}/edit")
    public String editWedstrijdSubmit(@PathVariable long wedstrijdId,
                                      @ModelAttribute("form") @Valid WedstrijdEditDTO form,
                                      BindingResult bindingResult,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        var wedstrijd = wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("wedstrijd", wedstrijd);
            model.addAttribute("locaties", wedstrijdService.getAllLocaties());
            return "wedstrijd-edit";
        }

        wedstrijdService.editWedstrijd(wedstrijdId, form);
        redirectAttributes.addFlashAttribute("successMatchUpdated", true);
        return "redirect:/wedstrijd";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{wedstrijdId}/edit-uitslag")
    public String editForm(@PathVariable long wedstrijdId, Model model) {
        var wedstrijd = wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId);
        WedstrijdUitslagDTO form = new WedstrijdUitslagDTO();
        form.setUitslagLand1(wedstrijd.getUitslagLand1());
        form.setUitslagLand2(wedstrijd.getUitslagLand2());
        model.addAttribute("wedstrijd", wedstrijd);
        model.addAttribute("form", form);
        return "wedstrijd-edit-uitslag";
    }

    /** Controleert invoer en het uitslagmoment en laat daarna de uitslag en scores bijwerken. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{wedstrijdId}/edit-uitslag")
    public String editSubmit(@PathVariable long wedstrijdId,
                             @ModelAttribute("form") @Valid WedstrijdUitslagDTO form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes)  {
        var wedstrijd = wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId);
        LocalDateTime start = LocalDateTime.of(wedstrijd.getSpeeldatum(), wedstrijd.getStarttijd().toLocalTime());
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start.plusHours(2))) {
            bindingResult.reject("uitslagLand1", "De uitslag kan pas ingevuld worden na 2 uur na de start van de wedstrijd.");
            bindingResult.reject("uitslagLand2", "De uitslag kan pas ingevuld worden na 2 uur na de start van de wedstrijd.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("wedstrijd", wedstrijd);
            return "wedstrijd-edit-uitslag";
        }
        try {
            wedstrijdService.updateUitslag(wedstrijdId, form);
        } catch (IllegalStateException ex) {
            model.addAttribute("wedstrijd", wedstrijd);
            model.addAttribute("magUitslag", LocalDateTime.now().isAfter(start.plusHours(2)));
            model.addAttribute("editError", ex.getMessage());
            return "wedstrijd-edit-uitslag";
        }
        redirectAttributes.addFlashAttribute("successMatchUpdated", true);

        return "redirect:/wedstrijd";
    }
}
