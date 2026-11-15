package com.example.fifa_2026.security;

import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Rol;
import com.example.fifa_2026.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrentUserService currentUserService;

    @BeforeEach
    void setup() {

        Gebruiker gebruiker = Gebruiker.builder()
                .rol(Rol.USER)
                .voornaam("an")
                .familienaam("aarts")
                .username("user")
                .password(new BCryptPasswordEncoder().encode("testPassword"))
                .score(0)
                .teams(null)
                .build();

        when(currentUserService.getCurrentUsernameOrThrow())
                .thenReturn(String.valueOf(gebruiker));
    }

    // =========================
    // PUBLIC PAGES
    // =========================

    @ParameterizedTest
    @CsvSource({
            "/login,login",
            "/wedstrijd,wedstrijdoverzicht"
    })
    void publicPagesAccessible(
            String url,
            String expectedView
    ) throws Exception {

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedView));
    }

    // =========================
    // ANONYMOUS USER
    // =========================

    @WithAnonymousUser
    @ParameterizedTest
    @CsvSource({"/", "/wedstrijd/"})
    void startUrlVerwijstNaarOpenbaarWedstrijdoverzicht(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wedstrijd"));
    }

    @WithAnonymousUser
    @Test
    void anonymousUserRedirectedToLogin() throws Exception {

        mockMvc.perform(get("/profiel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // =========================
    // USER ACCESS
    // =========================

    @ParameterizedTest
    @CsvSource({"supporter, USER", "beheerder, ADMIN"})
    void eigenApiProfielToontGebruikersnaamEnRol(String username, Rol rol) throws Exception {
        when(currentUserService.getCurrentGebruikerOrThrow())
                .thenReturn(Gebruiker.builder().username(username).rol(rol).build());
        when(currentUserService.isAdmin()).thenReturn(rol == Rol.ADMIN);

        mockMvc.perform(get("/api/profiel").with(user(username).roles(rol.name())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.rol").value(rol.name()));
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    void authenticatedUserCanAccessProtectedPage() throws Exception {

        mockMvc.perform(get("/wedstrijd/1"))
                .andExpect(status().isOk());
    }

    // =========================
    // ADMIN ACCESS
    // =========================

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    void adminCanAccessAdminPage() throws Exception {

        mockMvc.perform(get("/wedstrijd/nieuw"))
                .andExpect(status().isOk());
    }

    // =========================
    // USER FORBIDDEN
    // =========================

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    void userCannotAccessAdminPage() throws Exception {

        mockMvc.perform(get("/wedstrijd/nieuw"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "USER")
    @ParameterizedTest
    @CsvSource({"/wedstrijd/nieuw", "/api/beheer/wedstrijden", "/wedstrijd/1/delete"})
    void gewoneGebruikerKanWedstrijdenNietBeheren(String url) throws Exception {
        mockMvc.perform(post(url).with(csrf()))
                .andExpect(status().isForbidden());
    }

}
