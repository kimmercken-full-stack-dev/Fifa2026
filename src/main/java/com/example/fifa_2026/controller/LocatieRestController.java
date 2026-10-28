package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.LocatieDTO;
import com.example.fifa_2026.service.LocatieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/locaties")
public class LocatieRestController {
    private final LocatieService locatieService;

    /** Geeft locatie-ID en stadioncapaciteit terug als JSON voor het opgegeven locatie-ID. */
    @GetMapping("/{id}/capaciteit")
    public LocatieDTO getById(@PathVariable Long id) {
        return locatieService.getLocatieById(id);
    }

}
