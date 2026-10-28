package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.*;
import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Locatie;
import com.example.fifa_2026.model.Prognose;
import com.example.fifa_2026.model.Wedstrijd;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.LocatieService;
import com.example.fifa_2026.service.PrognoseService;
import com.example.fifa_2026.service.WedstrijdService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class WedstrijdControllerTest {

    @InjectMocks
    WedstrijdController controller;

    @Mock
    WedstrijdService wedstrijdService;

    @Mock
    PrognoseService prognoseService;

    @Mock
    CurrentUserService currentUserService;

    @Mock
    LocatieService locatieService;

    @Mock
    Model model;

    @Mock
    BindingResult bindingResult;

    @Mock
    RedirectAttributes redirectAttributes;

    WedstrijdDTO maakWedstrijdDTO(long id, String land1, String land2, LocalDate datum, String tijd) {
        return new WedstrijdDTO(id, land1, land2, datum, Time.valueOf(tijd),
                null, null, null, "StadX",
                "StadionX", 1070, 3);
    }

    Wedstrijd maakWedstrijdEntity(long id, String land1, String land2, LocalDate datum, String tijd, Integer uitslag1, Integer uitslag2) {
        Wedstrijd w = new Wedstrijd();
        w.setWedstrijdID(id);
        w.setLand1(land1);
        w.setLand2(land2);
        w.setSpeeldatum(datum);
        w.setStarttijd(Time.valueOf(tijd));
        w.setUitslagLand1(uitslag1);
        w.setUitslagLand2(uitslag2);
        Locatie locatie = new Locatie("Brussel", "Koning Boudewijnstadion", 1234, 10000); // of laat leeg als je dat nooit test
        return w;
    }

    Prognose maakPrognoseEntity(int id, int uitslagLand1, int uitslagLand2) {
        Prognose p = new Prognose();
        p.setId(id);
        p.setUitslagLand1(uitslagLand1);
        p.setUitslagLand2(uitslagLand2);
        return p;
    }

    @Test
    void listGuest_addsAllAttributesAndReturnsView() {
        List<WedstrijdDTO> wedstrijden = List.of(
                maakWedstrijdDTO(1, "NL", "BE", LocalDate.now().plusDays(1), "20:00:00"),
                maakWedstrijdDTO(2, "DE", "FR", LocalDate.now().minusDays(1), "21:00:00"));
        when(wedstrijdService.getAllWedstrijden()).thenReturn(wedstrijden);
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(7L);
        when(prognoseService.getPrognoseMapForGebruiker(7L)).thenReturn(Map.of(1L, maakPrognoseEntity(15, 3, 2)));
        String view = controller.listGuest(model);
        verify(model).addAttribute(eq("allWedstrijden"), eq(wedstrijden));
        verify(model).addAttribute(eq("canEditMap"), any(Map.class));
        verify(model).addAttribute(eq("canCreatePrognoseMap"), any(Map.class));
        verify(model).addAttribute(eq("prognoseMap"), any(Map.class));
        assertEquals("wedstrijdoverzicht", view);
    }

    @Test
    void wedstrijdDetail_addsAttributesAndReturnsDetailView() {
        long wedstrijdId = 42L;
        Wedstrijd wedstrijd = maakWedstrijdEntity(wedstrijdId, "NL", "BE", LocalDate.now().plusDays(1),
                "21:00:00", null, null);
        when(wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId)).thenReturn(wedstrijd);
        long gebruikerId = 3L;
        when(currentUserService.getCurrentGebruikerIdOrThrow()).thenReturn(gebruikerId);
        PrognoseFormDTO eigenPrognose = new PrognoseFormDTO();
        eigenPrognose.setWedstrijdId(wedstrijdId);
        eigenPrognose.setUitslagLand1(1);
        eigenPrognose.setUitslagLand2(0);
        when(prognoseService.getPrognoseFormForWedstrijdAndGebruiker(wedstrijdId, gebruikerId)).thenReturn(eigenPrognose);
        String view = controller.wedstrijdDetail(wedstrijdId, model);
        verify(model).addAttribute(eq("wedstrijd"), eq(wedstrijd));
        verify(model).addAttribute(eq("eigenPrognose"), eq(eigenPrognose));
        verify(model).addAttribute(eq("prognoseForm"), any(PrognoseFormDTO.class));
        verify(model).addAttribute(eq("magWijzigenPrognose"), anyBoolean());
        assertEquals("wedstrijd-detail", view);
    }

    @Test
    void createForm_addsAttributesAndReturnsView() {
        List<Locatie> locaties = List.of(new Locatie("Brussel", "Koning Boudewijnstadion", 1234, 10000));
        when(wedstrijdService.getAllLocaties()).thenReturn(locaties);
        String view = controller.createForm(model);
        verify(model).addAttribute(eq("form"), any(WedstrijdCreateDTO.class));
        verify(model).addAttribute(eq("locaties"), eq(locaties));
        assertEquals("wedstrijd-nieuw", view);
    }

    @Test
    void create_withValidationErrors_returnsFormAgain() {
        when(bindingResult.hasErrors()).thenReturn(true);
        List<Locatie> locaties = List.of(new Locatie("Brussel", "Koning Boudewijnstadion", 1234, 10000));
        when(wedstrijdService.getAllLocaties()).thenReturn(locaties);
        WedstrijdCreateDTO form = new WedstrijdCreateDTO();
        String view = controller.create(form, bindingResult, model, redirectAttributes);
        verify(model).addAttribute(eq("locaties"), eq(locaties));
        assertEquals("wedstrijd-nieuw", view);
        verify(wedstrijdService, never()).createWedstrijd(any());
    }

    @Test
    void create_valid_callsServiceAndRedirects() {
        when(bindingResult.hasErrors()).thenReturn(false);
        WedstrijdCreateDTO form = new WedstrijdCreateDTO();
        Wedstrijd wedstrijd = new Wedstrijd();
        when(wedstrijdService.createWedstrijd(form)).thenReturn(wedstrijd);
        String view = controller.create(form, bindingResult, model, redirectAttributes);
        verify(wedstrijdService).createWedstrijd(form);
        verify(redirectAttributes).addFlashAttribute("successMatchCreated", true);
        assertEquals("redirect:/wedstrijd", view);
    }

    @Test
    void editForm_loadsWedstrijdAndReturnsView() {
        long wedstrijdId = 77L;
        Wedstrijd wedstrijd = maakWedstrijdEntity(wedstrijdId, "AA", "BB",
                LocalDate.now(), "20:00:00", 2, 1);
        when(wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId)).thenReturn(wedstrijd);
        String view = controller.editForm(wedstrijdId, model);
        verify(model).addAttribute("wedstrijd", wedstrijd);
        verify(model).addAttribute(eq("form"), any(WedstrijdUitslagDTO.class));
        assertEquals("wedstrijd-edit-uitslag", view);
    }

    @Test
    void editSubmit_tooEarly_addsBindingErrorAndReturnsForm() {
        long wedstrijdId = 12L;
        // Future start, dus now.isBefore(start.plusHours(2)) zal true zijn
        Wedstrijd wedstrijd = maakWedstrijdEntity(wedstrijdId, "AAA", "BBB",
                LocalDate.now().plusDays(5), "20:00:00", null, null);
        when(wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId)).thenReturn(wedstrijd);
        when(bindingResult.hasErrors()).thenReturn(true); // Er zijn (dan) errors
        WedstrijdUitslagDTO dto = new WedstrijdUitslagDTO();
        String view = controller.editSubmit(wedstrijdId, dto, bindingResult, model, redirectAttributes);
        verify(model).addAttribute("wedstrijd", wedstrijd);
        assertEquals("wedstrijd-edit-uitslag", view);
    }

    @Test
    void editSubmit_valid_updateAndRedirect() {
        long wedstrijdId = 13L;
        Wedstrijd wedstrijd = maakWedstrijdEntity(wedstrijdId, "P", "Q",
                LocalDate.now().minusDays(5), "20:00:00", 1, 2);
        when(wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId)).thenReturn(wedstrijd);
        when(bindingResult.hasErrors()).thenReturn(false);
        WedstrijdUitslagDTO dto = new WedstrijdUitslagDTO();
        // Mag geen exception gooien:
        doNothing().when(wedstrijdService).updateUitslag(wedstrijdId, dto);
        String view = controller.editSubmit(wedstrijdId, dto, bindingResult, model, redirectAttributes);
        verify(wedstrijdService).updateUitslag(wedstrijdId, dto);
        assertEquals("redirect:/wedstrijd", view);
    }

    @Test
    void editSubmit_serviceThrowsException_addsEditErrorAndReturnsForm() {
        long wedstrijdId = 14L;
        Wedstrijd wedstrijd = maakWedstrijdEntity(wedstrijdId, "X", "Y",
                LocalDate.now().minusDays(2), "20:00:00", 0, 0);
        when(wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId)).thenReturn(wedstrijd);
        when(bindingResult.hasErrors()).thenReturn(false);
        WedstrijdUitslagDTO dto = new WedstrijdUitslagDTO();
        doThrow(new IllegalStateException("Niet toegestaan")).when(wedstrijdService).updateUitslag(wedstrijdId, dto);
        String view = controller.editSubmit(wedstrijdId, dto, bindingResult, model, redirectAttributes);
        verify(model).addAttribute("wedstrijd", wedstrijd);
        verify(model).addAttribute(eq("magUitslag"), anyBoolean());
        verify(model).addAttribute(eq("editError"), eq("Niet toegestaan"));
        assertEquals("wedstrijd-edit-uitslag", view);
    }
    @Test
    void deleteWedstrijd_callsServiceAndRedirects() {
        long wedstrijdId = 55L;
        String view = controller.deleteWedstrijd(wedstrijdId, redirectAttributes);
        verify(wedstrijdService).deleteWedstrijd(wedstrijdId);
        verify(redirectAttributes).addFlashAttribute("successMatchDeleted", true);
        assertEquals("redirect:/wedstrijd", view);
    }

    @Test
    void editWedstrijdForm_loadsWedstrijdAndReturnsView() {
        long wedstrijdId = 88L;
        Wedstrijd wedstrijd = maakWedstrijdEntity(wedstrijdId, "BE", "NL",
                LocalDate.now().plusDays(2), "20:00:00", null, null);

        Locatie locatie = new Locatie("Brussel", "Koning Boudewijnstadion", 1000, 50000);
        locatie.setId(9L);
        wedstrijd.setLocatie(locatie);
        List<Locatie> locaties = List.of(locatie);
        when(wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId)).thenReturn(wedstrijd);
        when(wedstrijdService.getAllLocaties()).thenReturn(locaties);
        String view = controller.editWedstrijdForm(wedstrijdId, model);
        verify(model).addAttribute("locaties", locaties);
        verify(model).addAttribute("wedstrijd", wedstrijd);
        verify(model).addAttribute(eq("form"), any(WedstrijdEditDTO.class));
        assertEquals("wedstrijd-edit", view);
    }

    @Test
    void editWedstrijdSubmit_withValidationErrors_returnsFormAgain() {
        long wedstrijdId = 91L;

        Wedstrijd wedstrijd = maakWedstrijdEntity(wedstrijdId, "FR", "DE",
                LocalDate.now().plusDays(1), "21:00:00", null, null);

        List<Locatie> locaties = List.of(new Locatie("Brussel", "Heysel", 1000, 40000));
        when(wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId)).thenReturn(wedstrijd);
        when(wedstrijdService.getAllLocaties()).thenReturn(locaties);
        when(bindingResult.hasErrors()).thenReturn(true);
        WedstrijdEditDTO dto = new WedstrijdEditDTO();

        String view = controller.editWedstrijdSubmit(wedstrijdId, dto, bindingResult,
                model, redirectAttributes);
        verify(model).addAttribute("wedstrijd", wedstrijd);
        verify(model).addAttribute("locaties", locaties);
        verify(wedstrijdService, never()).editWedstrijd(anyLong(), any());
        assertEquals("wedstrijd-edit", view);
    }

    @Test
    void editWedstrijdSubmit_valid_updatesAndRedirects() {
        long wedstrijdId = 92L;
        Wedstrijd wedstrijd = maakWedstrijdEntity(wedstrijdId, "ES", "PT", LocalDate.now().plusDays(3),
                "18:00:00", null, null);

        when(wedstrijdService.getWedstrijdEntityOrThrow(wedstrijdId)).thenReturn(wedstrijd);
        when(bindingResult.hasErrors()).thenReturn(false);
        WedstrijdEditDTO dto = new WedstrijdEditDTO();
        dto.setLand1("ES");
        dto.setLand2("PT");
        dto.setSpeeldatum("2026-06-15");
        dto.setStarttijd("18:00");
        dto.setLocatieId(1L);
        String view = controller.editWedstrijdSubmit(wedstrijdId, dto,
                bindingResult, model, redirectAttributes);

        verify(wedstrijdService).editWedstrijd(wedstrijdId, dto);
        verify(redirectAttributes).addFlashAttribute("successMatchUpdated", true);
        assertEquals("redirect:/wedstrijd", view);
    }
}
