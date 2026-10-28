package com.example.fifa_2026.restController;

import com.example.fifa_2026.controller.LocatieRestController;
import com.example.fifa_2026.dto.LocatieDTO;
import com.example.fifa_2026.service.LocatieService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocatieRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocatieRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocatieService locatieService;

    @Test
    void getById_returnsLocatieDTO() throws Exception {
        // Arrange
        Long id = 1L;
        LocatieDTO locatie = new LocatieDTO(id, 40000);
        Mockito.when(locatieService.getLocatieById(id)).thenReturn(locatie);

        // Act & Assert
        mockMvc.perform(get("/locaties/{id}/capaciteit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.capaciteit").value(40000));

        Mockito.verify(locatieService).getLocatieById(id);
    }
}