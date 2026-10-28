package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.TeamsDTO;
import com.example.fifa_2026.service.TeamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TeamControllerTest {

    @InjectMocks
    TeamController controller;

    @Mock
    TeamService teamService;

    @Mock
    Model model;

    @Test
    void teams_addsTeamsAttributeAndReturnsView() {
        List<TeamsDTO> teams = List.of(
                new TeamsDTO(1L, "Alpha", "Bob", 34, 11),
                new TeamsDTO(2L, "Beta", "Els", 27, 7)
        );
        when(teamService.getTop10Teams()).thenReturn(teams);
        String view = controller.teams(model);
        verify(model).addAttribute("teams", teams);
        assertEquals("teamoverzicht", view);
    }
}