package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.PrognoseFormDTO;
import com.example.fifa_2026.model.Prognose;
import com.example.fifa_2026.model.Wedstrijd;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.PrognoseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.sql.Time;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PrognoseControllerTest {

    @InjectMocks
    PrognoseController controller;

    @Mock
    PrognoseService prognoseService;

    @Mock
    CurrentUserService currentUserService;

    @Mock
    Model model;

    @Mock
    BindingResult bindingResult;

    // Helper om een dummy Wedstrijd te maken
    Wedstrijd dummyWedstrijd() {
        Wedstrijd w = new Wedstrijd();
        w.setWedstrijdID(123L);
        w.setLand1("Nederland");
        w.setLand2("België");
        w.setSpeeldatum(LocalDate.of(2026, 6, 10));
        w.setStarttijd(Time.valueOf("18:00:00"));
        return w;
    }

    @Test
    void showForm_addsModelAndReturnsView() {
        long gebruikerId = 4L;
        long wedstrijdId = 77L;
        PrognoseFormDTO form = new PrognoseFormDTO();
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(gebruikerId);
        when(prognoseService.getWedstrijdOrThrow(wedstrijdId)).thenReturn(dummyWedstrijd());
        when(prognoseService.getPrognoseFormForWedstrijdAndGebruiker(wedstrijdId, gebruikerId)).thenReturn(form);
        String view = controller.showForm(wedstrijdId, model);
        verify(model).addAttribute(eq("wedstrijd"), any(Wedstrijd.class));
        verify(model).addAttribute("prognoseForm", form);
        verify(model).addAttribute("eigenPrognose", form);
        verify(model).addAttribute("magWijzigenPrognose", true);
        assertEquals("prognose-form-page", view);
    }

    @Test
    void submit_withValidationErrors_returnsFormAgain() {
        PrognoseFormDTO form = new PrognoseFormDTO();
        form.setWedstrijdId(10L);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(prognoseService.getWedstrijdOrThrow(form.getWedstrijdId())).thenReturn(dummyWedstrijd());
        String view = controller.submit(form, bindingResult, model);
        verify(model).addAttribute(eq("wedstrijd"), any(Wedstrijd.class));
        verify(model).addAttribute("prognoseForm", form);
        assertEquals("prognose-form-page", view);
        verify(prognoseService, never()).saveOrUpdatePrognose(any(), anyLong());
    }

    @Test
    void submit_withBusinessError_returnsFormWithError() {
        PrognoseFormDTO form = new PrognoseFormDTO();
        form.setWedstrijdId(20L);
        form.setUitslagLand1(2);
        form.setUitslagLand2(2);
        long gebruikerId = 17L;
        when(bindingResult.hasErrors()).thenReturn(false);
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(gebruikerId);
        doThrow(new IllegalStateException("Te laat!")).when(prognoseService).saveOrUpdatePrognose(form, gebruikerId);
        when(prognoseService.getWedstrijdOrThrow(form.getWedstrijdId())).thenReturn(dummyWedstrijd());
        String view = controller.submit(form, bindingResult, model);
        verify(model).addAttribute(eq("wedstrijd"), any(Wedstrijd.class));
        verify(model).addAttribute("prognoseForm", form);
        verify(model).addAttribute(eq("error"), eq("Te laat!"));
        assertEquals("prognose-form-page", view);
        verify(prognoseService).saveOrUpdatePrognose(form, gebruikerId);
    }

    @Test
    void submit_success_returnsRedirect() {
        PrognoseFormDTO form = new PrognoseFormDTO();
        form.setWedstrijdId(27L);
        form.setUitslagLand1(1);
        form.setUitslagLand2(0);
        long gebruikerId = 8L;
        when(bindingResult.hasErrors()).thenReturn(false);
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(gebruikerId);
        String view = controller.submit(form, bindingResult, model);
        assertEquals("redirect:/prognoses", view);
        verify(prognoseService).saveOrUpdatePrognose(form, gebruikerId);
    }
}
