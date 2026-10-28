package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.TeamCreateDTO;
import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Rol;
import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TeamManageControllerTest {

    @InjectMocks
    TeamManageController controller;

    @Mock
    TeamService teamService;

    @Mock
    CurrentUserService currentUserService;

    @Mock
    Model model;

    @Mock
    BindingResult bindingResult;

    Team dummyTeam(long id, long eigenaarId, String naam) {
        Gebruiker eigenaar = new Gebruiker(
                Rol.USER,            // rol (of Rol.ADMIN, ...)
                "Piet",              // voornaam
                "Jansen",            // familienaam
                "pietjans",          // username (uniek!)
                0,                   // score (bijv. 0)
                "testwachtwoord"     // wachtwoord (mag teststring zijn in unit-test)
        );
        Team t = new Team("Ajax", 12345678L, eigenaar);
        t.setTeamID(id);
        t.setTeamNaam(naam);
        eigenaar.setId(eigenaarId);
        t.setEigenaar(eigenaar);
        return t;
    }

    @Test
    void teamPage_setsModelAndReturnsView() {
        long teamId = 77L;
        long userId = 99L;
        Team t = dummyTeam(teamId, userId, "Testteam");
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(userId);
        when(teamService.getTeamForViewOrThrow(teamId, userId)).thenReturn(t);
        String view = controller.teamPage(teamId, model);
        verify(model).addAttribute("team", t);
        verify(model).addAttribute("isOwner", true);
        verify(model).addAttribute("currentUserId", userId);
        verify(model).addAttribute("joinError", null);
        assertEquals("teampagina", view);
    }

    @Test
    void nieuwTeamForm_addsEmptyFormAndReturnsView() {
        String view = controller.nieuwTeamForm(model);
        verify(model).addAttribute(eq("form"), any(TeamCreateDTO.class));
        assertEquals("team-nieuw", view);
    }

    @Test
    void nieuwTeamSubmit_withValidationError_returnsFormAgain() {
        when(bindingResult.hasErrors()).thenReturn(true);
        TeamCreateDTO dto = new TeamCreateDTO();
        String view = controller.nieuwTeamSubmit(dto, bindingResult);
        assertEquals("team-nieuw", view);
        verify(bindingResult).hasErrors();
        verifyNoInteractions(teamService, currentUserService);
    }

    @Test
    void nieuwTeamSubmit_valid_createsTeamAndRedirects() {
        when(bindingResult.hasErrors()).thenReturn(false);
        long userId = 10L;
        TeamCreateDTO form = new TeamCreateDTO();
        form.setTeamNaam("Ajax");
        Team created = dummyTeam(11L, userId, "Ajax");
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(userId);
        when(teamService.createTeam(eq("Ajax"), eq(userId))).thenReturn(created);
        String expectedRedirect = "redirect:/teams/nieuw?created=1&name=" +
                URLEncoder.encode("Ajax", StandardCharsets.UTF_8);
        String view = controller.nieuwTeamSubmit(form, bindingResult);
        assertEquals(expectedRedirect, view);
    }

    @Test
    void removeMember_callsServiceAndRedirects() {
        long teamId = 5L, userId = 17L, currentUserId = 9L;
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(currentUserId);
        String view = controller.removeMember(teamId, userId);
        verify(teamService).removeMember(teamId, userId, currentUserId);
        assertEquals("redirect:/teams/" + teamId, view);
    }

    @Test
    void regenerateInvite_callsServiceAndRedirects() {
        long teamId = 3L, currentUserId = 120L;
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(currentUserId);
        String view = controller.regenerateInvite(teamId);
        verify(teamService).regenerateInvitecode(teamId, currentUserId);
        assertEquals("redirect:/teams/" + teamId, view);
    }

    @Test
    void joinByCode_callsServiceAndRedirects() {
        long code = 12345L, currentUserId = 33L;
        Team t = dummyTeam(7L, 33L, "FC Bravo");
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(currentUserId);
        when(teamService.joinTeamByInvitecode(code, currentUserId)).thenReturn(t);
        String view = controller.joinByCode(code);
        verify(teamService).joinTeamByInvitecode(code, currentUserId);
        assertEquals("redirect:/teams/" + t.getTeamID(), view);
    }
}