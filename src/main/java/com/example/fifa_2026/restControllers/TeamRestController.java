package com.example.fifa_2026.restControllers;

import com.example.fifa_2026.dto.MijnTeamsoverzichtDTO;
import com.example.fifa_2026.dto.TeamCreateDTO;
import com.example.fifa_2026.dto.TeamsDTO;
import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.TeamScoreboardService;
import com.example.fifa_2026.service.TeamService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    record Lid(long id, String username, int score, boolean eigenaar) {}
    record TeamDetail(long teamId, String teamNaam, Long invitecode, boolean eigenaar, List<Lid> leden) {}
    record Uitnodiging(@Positive long code) {}

    @GetMapping("/teams")
    public List<TeamsDTO> topTeams() {
        return teams.getTop10Teams();
    }

    @GetMapping("/mijn-teams")
    public List<TeamsDTO> mijnTeams() {
        return scoreboard.getMyTeams(gebruikerId()).stream().map(t -> new TeamsDTO(t.getTeamID(),
                t.getTeamNaam(), t.getEigenaar().getUsername(), t.getScore(), t.getTeamleden().size())).toList();
    }

    @GetMapping("/teams/{id}")
    public TeamDetail team(@PathVariable long id) {
        long gebruiker = gebruikerId();
        Team team;
        try {
            team = teams.getTeamForViewOrThrow(id, gebruiker);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }
        boolean eigenaar = team.getEigenaar() != null && team.getEigenaar().getId() == gebruiker;
        return new TeamDetail(id, team.getTeamNaam(), eigenaar ? team.getInvitecode() : null, eigenaar,
                team.getTeamleden().stream().map(l -> new Lid(l.getId(), l.getUsername(), l.getScore(),
                        team.getEigenaar() != null && team.getEigenaar().getId() == l.getId())).toList());
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
        try {
            return scoreboard.getTeamScoreboard(id, gebruikerId());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        }
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
