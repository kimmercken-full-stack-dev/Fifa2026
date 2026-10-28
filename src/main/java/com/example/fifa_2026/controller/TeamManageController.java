package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.TeamCreateDTO;
import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamManageController {

    private final TeamService teamService;
    private final CurrentUserService currentUserService;

    /** Laadt een toegankelijk team en geeft aan de view door of de huidige gebruiker eigenaar is. */
    @GetMapping("/{id}")
    public String teamPage(@PathVariable long id, Model model) {
        long userId = currentUserService.getCurrentGebruikerIdOrThrow();

        Team team = teamService.getTeamForViewOrThrow(id, userId);
        boolean isOwner = team.getEigenaar() != null && team.getEigenaar().getId() == userId;

        model.addAttribute("team", team);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("currentUserId", userId);
        model.addAttribute("joinError", null);
        return "teampagina";
    }

    // ---- NIEUW TEAM ----

    @GetMapping("/nieuw")
    public String nieuwTeamForm(Model model) {
        model.addAttribute("form", new TeamCreateDTO());
        return "team-nieuw";
    }

    /** Verwerkt het teamformulier en verwijst na aanmaken door met de teamnaam voor de bevestiging. */
    @PostMapping("/nieuw")
    public String nieuwTeamSubmit(@ModelAttribute("form") @Valid TeamCreateDTO form,
                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "team-nieuw";
        }

        long userId = currentUserService.getCurrentGebruikerIdOrThrow();
        Team created = teamService.createTeam(form.getTeamNaam().trim(), userId);

        // popup op team-nieuw pagina
        return "redirect:/teams/nieuw?created=1&name=" +
                java.net.URLEncoder.encode(created.getTeamNaam(), java.nio.charset.StandardCharsets.UTF_8);
    }

    // ---- LEDEN BEHEREN ----

    /** Geeft de verwijderactie samen met de identiteit van de aanvrager door aan de service. */
    @PostMapping("/{teamId}/remove/{userId}")
    public String removeMember(@PathVariable long teamId,
                               @PathVariable long userId) {
        long currentUserId = currentUserService.getCurrentGebruikerIdOrThrow();
        teamService.removeMember(teamId, userId, currentUserId);
        return "redirect:/teams/" + teamId;
    }

    /** Laat de service de uitnodigingscode vernieuwen en keert terug naar de teampagina. */
    @PostMapping("/{teamId}/invite/regenerate")
    public String regenerateInvite(@PathVariable long teamId) {
        long currentUserId = currentUserService.getCurrentGebruikerIdOrThrow();
        teamService.regenerateInvitecode(teamId, currentUserId);
        return "redirect:/teams/" + teamId;
    }

    /** Laat de ingelogde gebruiker via een uitnodigingscode aansluiten bij een team. */
    @PostMapping("/join")
    public String joinByCode(@RequestParam("code") long code) {
        long currentUserId = currentUserService.getCurrentGebruikerIdOrThrow();
        Team joined = teamService.joinTeamByInvitecode(code, currentUserId);
        return "redirect:/teams/" + joined.getTeamID();
    }
}