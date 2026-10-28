package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.GebruikerRegistratieDTO;
import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Rol;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.GebruikerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GebruikerController.class)
@AutoConfigureMockMvc(addFilters = false)
class GebruikerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    GebruikerService gebruikerService;
    @MockitoBean
    CurrentUserService currentUserService;

    @Test
    void showRegister_displaysRegisterForm() throws Exception {
        mockMvc.perform(get("/registreer"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("form"))
                .andExpect(view().name("registreer"));
    }

    @Test
    void doRegister_invalidInput_returnsFormWithErrors() throws Exception {
        // username te kort, wachtwoorden niet gelijk
        mockMvc.perform(post("/registreer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("voornaam", "J")
                        .param("familienaam", "X")
                        .param("username", "a")
                        .param("password", "mismatched")
                        .param("confirmPassword", "different")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("registreer"))
                .andExpect(model().hasErrors());
        // service mag niet aangeroepen zijn
        Mockito.verify(gebruikerService, Mockito.never()).registreer(any());
    }

    @Test
    void doRegister_duplicateUsername_showsRegisterError() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Nickname is al in gebruik: testuser"))
                .when(gebruikerService).registreer(any(GebruikerRegistratieDTO.class));

        mockMvc.perform(post("/registreer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("voornaam", "Jan")
                        .param("familienaam", "Jansen")
                        .param("username", "testuser")
                        .param("password", "secretpass")
                        .param("confirmPassword", "secretpass")
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("registerError"))
                .andExpect(view().name("registreer"));

        Mockito.verify(gebruikerService).registreer(any());
    }

    @Test
    void doRegister_success_redirectsToLogin() throws Exception {
        Mockito.when(gebruikerService.registreer(any(GebruikerRegistratieDTO.class)))
                .thenReturn(new Gebruiker(Rol.USER, "Piet", "Jansen", "pietje123", 0, "geheim123"));

        mockMvc.perform(post("/registreer")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("voornaam", "Piet")
                        .param("familienaam", "Jansen")
                        .param("username", "pietje123")
                        .param("password", "goedgeheim123")
                        .param("confirmPassword", "goedgeheim123")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        Mockito.verify(gebruikerService).registreer(any());
    }
}