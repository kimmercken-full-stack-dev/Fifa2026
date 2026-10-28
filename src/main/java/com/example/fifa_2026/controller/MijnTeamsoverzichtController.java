package com.example.fifa_2026.controller;

import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.TeamScoreboardService;
import com.example.fifa_2026.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MijnTeamsoverzichtController {
    private final TeamService teamService;
    private final TeamScoreboardService teamScoreboardService;
    private final CurrentUserService currentUserService;

    @GetMapping("/mijn-teams")
    public String mijnTeams(Model model) {
        long gebruikerId = currentUserService.getCurrentGebruikerIdOrThrow();
        model.addAttribute("teams", teamScoreboardService.getMyTeams(gebruikerId));
        return "mijn-teams";
    }
    @PostMapping("/teams/{teamId}/delete")
    public String deleteTeam(@PathVariable long teamId) {

        long gebruikerId =
                currentUserService.getCurrentGebruikerIdOrThrow();

        teamService.deleteTeam(teamId, gebruikerId);

        return "redirect:/mijn-teams";
    }
    @GetMapping("/teams/{teamId}/scoreboard")
    public String teamScoreboard(@PathVariable long teamId, Model model) {
        long gebruikerId = currentUserService.getCurrentGebruikerIdOrThrow();
        model.addAttribute("scoreboard", teamScoreboardService.getTeamScoreboard(teamId, gebruikerId));
        return "team-scoreboard";
    }
}