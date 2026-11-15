package com.example.fifa_2026.restControllers;

import com.example.fifa_2026.dto.MijnTeamsoverzichtDTO;
import com.example.fifa_2026.dto.TeamCreateDTO;
import com.example.fifa_2026.dto.TeamsDTO;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.TeamScoreboardService;
import com.example.fifa_2026.service.TeamService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TeamRestController {
    private final CurrentUserService currentUser;
    private final TeamService teams;
    private final TeamScoreboardService scoreboard;

    record Uitnodiging(@Positive long code) {}

    @GetMapping("/teams")
    public List<TeamsDTO> topTeams() {
        return teams.getTop10Teams();
    }

    @GetMapping("/mijn-teams")
    public List<TeamsDTO> mijnTeams() {
        return scoreboard.getMijnTeamsOverzicht(gebruikerId());
    }

    @GetMapping("/teams/{id}")
    public MijnTeamsoverzichtDTO team(@PathVariable long id) {
        return teams.getTeamDetail(id, gebruikerId());
    }

    @PostMapping("/teams")
    public long maakTeam(@Valid @RequestBody TeamCreateDTO form) {
        return teams.createTeam(form.getTeamNaam(), gebruikerId()).getTeamID();
    }

    @PostMapping("/teams/join")
    public long sluitAan(@Valid @RequestBody Uitnodiging form) {
        return teams.joinTeamByInvitecode(form.code(), gebruikerId()).getTeamID();
    }

    @GetMapping("/teams/{id}/scoreboard")
    public MijnTeamsoverzichtDTO klassement(@PathVariable long id) {
        return scoreboard.getTeamScoreboard(id, gebruikerId());
    }

    @DeleteMapping("/teams/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verwijderTeam(@PathVariable long id) {
        teams.deleteTeam(id, gebruikerId());
    }

    @DeleteMapping("/teams/{id}/leden/{lid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verwijderLid(@PathVariable long id, @PathVariable long lid) {
        teams.removeMember(id, lid, gebruikerId());
    }

    @PostMapping("/teams/{id}/invite")
    public long vernieuwCode(@PathVariable long id) {
        return teams.regenerateInvitecode(id, gebruikerId());
    }

    private long gebruikerId() {
        return currentUser.getCurrentGebruikerIdOrThrow();
    }
}
