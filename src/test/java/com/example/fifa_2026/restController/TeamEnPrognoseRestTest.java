package com.example.fifa_2026.restController;

import com.example.fifa_2026.config.SecurityConfig;
import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Locatie;
import com.example.fifa_2026.model.Prognose;
import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.model.Wedstrijd;
import com.example.fifa_2026.repository.GebruikerRepository;
import com.example.fifa_2026.repository.PrognoseRepository;
import com.example.fifa_2026.repository.TeamRepository;
import com.example.fifa_2026.repository.WedstrijdRepository;
import com.example.fifa_2026.restControllers.PrognoseRestController;
import com.example.fifa_2026.restControllers.TeamRestController;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.PrognoseService;
import com.example.fifa_2026.service.TeamScoreboardService;
import com.example.fifa_2026.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({TeamRestController.class, PrognoseRestController.class})
@Import({SecurityConfig.class, TeamService.class, TeamScoreboardService.class, PrognoseService.class})
@WithMockUser
class TeamEnPrognoseRestTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private CurrentUserService currentUser;
    @MockitoBean private GebruikerRepository gebruikers;
    @MockitoBean private TeamRepository teams;
    @MockitoBean private PrognoseRepository prognoses;
    @MockitoBean private WedstrijdRepository wedstrijden;

    @BeforeEach
    void stelGebruikerIn() {
        when(currentUser.getCurrentGebruikerIdOrThrow()).thenReturn(1L);
    }

    @Test
    void teamdetailToontUitnodigingscodeAlleenAanDeEigenaar() throws Exception {
        var eigenaar = Gebruiker.builder().id(1L).username("eigenaar").build();
        var lid = Gebruiker.builder().id(2L).username("lid").build();
        var team = new Team("Supporters", 12345678L, eigenaar);
        team.setTeamID(3L);
        team.getTeamleden().addAll(List.of(eigenaar, lid));
        when(teams.findWithTeamledenByTeamID(3L)).thenReturn(Optional.of(team));

        mvc.perform(get("/api/teams/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(3))
                .andExpect(jsonPath("$.invitecode").value(12345678))
                .andExpect(jsonPath("$.leden.length()").value(2))
                .andExpect(jsonPath("$.leden[0].gebruikerId").value(1))
                .andExpect(jsonPath("$.leden[0].nickname").value("eigenaar"))
                .andExpect(jsonPath("$.leden[0].eigenaar").value(true));

        mvc.perform(get("/api/teams/3/scoreboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invitecode").doesNotExist())
                .andExpect(jsonPath("$.leden[0].eigenaar").doesNotExist());

        when(currentUser.getCurrentGebruikerIdOrThrow()).thenReturn(2L);
        mvc.perform(get("/api/teams/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eigenaar").value(false))
                .andExpect(jsonPath("$.invitecode").doesNotExist());

        when(currentUser.getCurrentGebruikerIdOrThrow()).thenReturn(4L);
        mvc.perform(get("/api/teams/3"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void ontbrekenVanTeamOfWedstrijdGeeft404() throws Exception {
        mvc.perform(get("/api/teams/3/scoreboard"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mvc.perform(get("/api/prognoses/3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void bestaandeTeamnaamGeeft409EnOngeldigeInvoerGeeft400() throws Exception {
        when(teams.existsByTeamNaamIgnoreCase("Supporters")).thenReturn(true);
        mvc.perform(post("/api/teams").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"teamNaam":"Supporters"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        mvc.perform(post("/api/teams").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fouten").isArray());
    }

    @Test
    void prognoseOverzichtEnOpslagGebruikenDezelfdeDeadline() throws Exception {
        var wedstrijd = new Wedstrijd();
        wedstrijd.setWedstrijdID(3L);
        wedstrijd.setLand1("Belgie");
        wedstrijd.setLand2("Nederland");
        wedstrijd.setSpeeldatum(LocalDate.now().minusDays(1));
        wedstrijd.setStarttijd(Time.valueOf("18:00:00"));
        wedstrijd.setUitslagLand1(0);
        wedstrijd.setUitslagLand2(1);
        wedstrijd.setWinnaar("Nederland");
        var locatie = new Locatie("Brussel", "Stadion", 1234, 50000);
        locatie.setChecksum(1234 % 97);
        wedstrijd.setLocatie(locatie);
        var prognose = new Prognose();
        prognose.setWedstrijd(wedstrijd);
        prognose.setUitslagLand1(2);
        prognose.setUitslagLand2(1);
        prognose.setWinnaar("Belgie");
        when(prognoses.findAllByGebruiker_IdOrderByWedstrijd_SpeeldatumAscWedstrijd_StarttijdAsc(1L))
                .thenReturn(List.of(prognose));
        when(wedstrijden.findById(3L)).thenReturn(Optional.of(wedstrijd));
        when(gebruikers.findById(1L)).thenReturn(Optional.of(Gebruiker.builder().id(1L).build()));

        mvc.perform(get("/api/prognoses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].wedstrijd.wedstrijdID").value(3))
                .andExpect(jsonPath("$[0].wedstrijd.stadion").value("Stadion"))
                .andExpect(jsonPath("$[0].wedstrijd.starttijd").value("18:00:00"))
                .andExpect(jsonPath("$[0].wedstrijd.uitslagLand1").value(0))
                .andExpect(jsonPath("$[0].wedstrijd.winnaar").value("Nederland"))
                .andExpect(jsonPath("$[0].wedstrijdId").value(3))
                .andExpect(jsonPath("$[0].uitslagLand1").value(2))
                .andExpect(jsonPath("$[0].winnaar").value("Belgie"))
                .andExpect(jsonPath("$[0].wijzigbaar").value(false));

        mvc.perform(get("/api/prognoses/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wedstrijdId").value(3))
                .andExpect(jsonPath("$.wedstrijd").doesNotExist())
                .andExpect(jsonPath("$.wijzigbaar").doesNotExist());

        mvc.perform(put("/api/prognoses/3").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"wedstrijdId":3,"uitslagLand1":2,"uitslagLand2":1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void onverwachteServicefoutGeeft500ZonderInterneDetails() throws Exception {
        when(teams.findWithTeamledenByTeamID(3L)).thenThrow(new IllegalStateException("interne fout"));
        mvc.perform(get("/api/teams/3"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Er is een onverwachte fout opgetreden."));
    }
}
