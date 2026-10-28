package com.example.fifa_2026.controller;

import com.example.fifa_2026.model.Prognose;
import com.example.fifa_2026.model.Wedstrijd;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.PrognoseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.ui.Model;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PrognoseOverzichtControllerTest {

    @InjectMocks
    PrognoseOverzichtController controller;

    @Mock
    PrognoseService prognoseService;

    @Mock
    CurrentUserService currentUserService;

    @Mock
    Model model;

    // Helper: altijd een geldige tijd ("HH:mm:ss")
    String formatTime(LocalDateTime dateTime) {
        return dateTime.toLocalTime().withNano(0).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    Prognose dummyPrognose(long prognoseId, LocalDate speeldatum, String tijd, long wedstrijdId) {
        Prognose p = new Prognose();
        p.setId(prognoseId);
        Wedstrijd w = new Wedstrijd();
        w.setSpeeldatum(speeldatum);
        w.setStarttijd(Time.valueOf(tijd));
        w.setWedstrijdID(wedstrijdId);
        p.setWedstrijd(w);
        return p;
    }

    @Test
    void overzicht_addsModelAttributesAndComputesCanEditMap() {
        long gebruikerId = 99L;
        LocalDateTime now = LocalDateTime.now();
        // Maak geldige tijdstrings ("HH:mm:ss"):
        String tijd1 = formatTime(now.plusHours(5));
        String tijd2 = formatTime(now.minusHours(3));
        Prognose p1 = dummyPrognose(1L, now.plusDays(1).toLocalDate(), tijd1, 10);
        Prognose p2 = dummyPrognose(2L, now.minusDays(1).toLocalDate(), tijd2, 20);
        List<Prognose> prognoses = List.of(p1, p2);
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(gebruikerId);
        when(prognoseService.getPrognosesForGebruiker(gebruikerId)).thenReturn(prognoses);
        ArgumentCaptor<java.util.Map<Long, Boolean>> mapCaptor = ArgumentCaptor.forClass(java.util.Map.class);
        String view = controller.overzicht(model);
        verify(model).addAttribute("prognoses", prognoses);
        verify(model).addAttribute(eq("canEditPrognoseMap"), mapCaptor.capture());
        var map = mapCaptor.getValue();
        assertTrue(map.containsKey(1L));
        assertTrue(map.containsKey(2L));
        assertEquals("prognose-overzicht", view);
    }

    @Test
    void delete_callsServiceAndRedirects() {
        long gebruikerId = 23L;
        long prognoseId = 8L;
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(gebruikerId);
        String view = controller.delete(prognoseId);
        verify(currentUserService).getCurrentGebruikerIdOrThrow();
        verify(prognoseService).deletePrognoseById(prognoseId, gebruikerId);
        assertEquals("redirect:/prognoses", view);
    }
}