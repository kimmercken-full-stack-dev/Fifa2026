package com.example.fifa_2026.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.LocaleResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Locale;

@WebMvcTest(LocaleController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocaleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LocaleResolver localeResolver;

    @Test
    void changeLocale_redirectsWithReferer_andSetsLocale() throws Exception {
        // Arrange
        String refererUrl = "/home";
        String lang = "nl";

        Mockito.doNothing().when(localeResolver).setLocale(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(Locale.forLanguageTag(lang)));

        mockMvc.perform(get("/changeLocale")
                        .param("lang", lang)
                        .header("Referer", refererUrl))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(refererUrl));

        // Controleer dat localeResolver.setLocale werd aangeroepen met correcte lang
        Mockito.verify(localeResolver).setLocale(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(Locale.forLanguageTag(lang)));
    }

    @Test
    void changeLocale_withNoReferer_redirectsToNull_andSetsLocale() throws Exception {
        String lang = "en";
        Mockito.doNothing().when(localeResolver).setLocale(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(Locale.forLanguageTag(lang)));

        mockMvc.perform(get("/changeLocale")
                        .param("lang", lang))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("null")); // want geen referer (optioneel, afhankelijk van jouw wensen)

        Mockito.verify(localeResolver).setLocale(any(HttpServletRequest.class), any(HttpServletResponse.class), eq(Locale.forLanguageTag(lang)));
    }
}