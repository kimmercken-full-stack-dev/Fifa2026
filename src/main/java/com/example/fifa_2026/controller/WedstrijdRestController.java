package com.example.fifa_2026.controller;

import com.example.fifa_2026.dto.WedstrijdDTO;
import com.example.fifa_2026.service.WedstrijdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/wedstrijden")
public class WedstrijdRestController {
    private final WedstrijdService wedstrijdService;

    // 1. Gefilterd op datum

    /** Geeft wedstrijd-DTO's als JSON terug voor de opgegeven speeldatum. */
    @GetMapping("/{speeldatum}")
    public List<WedstrijdDTO> getBySpeeldatum(@PathVariable String speeldatum) {
        return wedstrijdService.getWedstrijdenVoorDatum(parseSpeeldatum(speeldatum));
    }

    /** Accepteert twee datumformaten en geeft HTTP 400 wanneer beide omzettingen mislukken. */
    private LocalDate parseSpeeldatum(String speeldatum) {
        try {
            return LocalDate.parse(speeldatum);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(speeldatum, DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            } catch (DateTimeParseException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Gebruik datumformaat yyyy-MM-dd of dd-MM-yyyy");
            }
        }
    }
}
