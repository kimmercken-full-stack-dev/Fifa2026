package com.example.fifa_2026.dto;

import java.sql.Time;
import java.time.LocalDate;

public record WedstrijdDTO(
        long wedstrijdID,
        String land1,
        String land2,
        LocalDate speeldatum,
        Time starttijd,
        Integer uitslagLand1,
        Integer uitslagLand2,
        String winnaar,
        String stad,
        String stadion,
        int stadiumcode,
        int checksum
) {}