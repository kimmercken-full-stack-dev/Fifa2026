package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.MijnTeamsoverzichtDTO;
import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.TeamScoreboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MijnTeamsoverzichtControllerTest {

    @InjectMocks
    MijnTeamsoverzichtController controller;

    @Mock
    TeamScoreboardService teamScoreboardService;

    @Mock
    CurrentUserService currentUserService;

    @Mock
    Model model;

    @Test
    void mijnTeams_addsTeamsAttributeAndReturnsView() {
        long gebruikerId = 12L;
        List<Team> teams = List.of(new Team("Ajax"), new Team("PSV"));
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(gebruikerId);
        when(teamScoreboardService.getMyTeams(gebruikerId)).thenReturn(teams);
        String view = controller.mijnTeams(model);
        verify(model).addAttribute("teams", teams);
        assertEquals("mijn-teams", view);
    }

    @Test
    void teamScoreboard_addsScoreboardAttributeAndReturnsView() {
        long gebruikerId = 23L;
        long teamId = 7L;
        MijnTeamsoverzichtDTO scoreboardDTO = new MijnTeamsoverzichtDTO(teamId, "Ajax", "Jan", 42, List.of());
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(gebruikerId);
        when(teamScoreboardService.getTeamScoreboard(teamId, gebruikerId)).thenReturn(scoreboardDTO);
        String view = controller.teamScoreboard(teamId, model);
        verify(model).addAttribute("scoreboard", scoreboardDTO);
        assertEquals("team-scoreboard", view);
    }
}