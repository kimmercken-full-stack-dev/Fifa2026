package com.example.fifa_2026.restController;

import com.example.fifa_2026.controller.WedstrijdRestController;
import com.example.fifa_2026.dto.WedstrijdDTO;
import com.example.fifa_2026.service.WedstrijdService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WedstrijdRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class WedstrijdRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WedstrijdService wedstrijdService;

    @Test
    void getBySpeeldatum_ReturnsListOfWedstrijdDTO() throws Exception {
        // Arrange
        LocalDate datum = LocalDate.of(2026, 6, 10);
        List<WedstrijdDTO> wedstrijden = List.of(
                new WedstrijdDTO(
                        1L, "Nederland", "België", datum,
                        java.sql.Time.valueOf("18:00:00"), null, null, null,
                        "Amsterdam", "Johan Cruijff Arena", 1234, 42
                ),
                new WedstrijdDTO(
                        2L, "Duitsland", "Frankrijk", datum,
                        java.sql.Time.valueOf("21:00:00"), null, null, null,
                        "München", "Allianz Arena", 5678, 95
                )
        );
        Mockito.when(wedstrijdService.getWedstrijdenVoorDatum(datum))
                .thenReturn(wedstrijden);

        // Act & Assert
        mockMvc.perform(get("/wedstrijden/{speeldatum}", datum))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].land1").value("Nederland"))
                .andExpect(jsonPath("$[1].land2").value("Frankrijk"));

        Mockito.verify(wedstrijdService).getWedstrijdenVoorDatum(datum);
    }

    @Test
    void getBySpeeldatum_acceptsIsoDateString() throws Exception {
        LocalDate datum = LocalDate.of(2026, 6, 10);
        Mockito.when(wedstrijdService.getWedstrijdenVoorDatum(datum))
                .thenReturn(List.of());

        mockMvc.perform(get("/wedstrijden/2026-06-10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        Mockito.verify(wedstrijdService).getWedstrijdenVoorDatum(datum);
    }

    @Test
    void getBySpeeldatum_acceptsDisplayDateString() throws Exception {
        LocalDate datum = LocalDate.of(2026, 6, 10);
        Mockito.when(wedstrijdService.getWedstrijdenVoorDatum(datum))
                .thenReturn(List.of());

        mockMvc.perform(get("/wedstrijden/10-06-2026")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        Mockito.verify(wedstrijdService).getWedstrijdenVoorDatum(datum);
    }
}
