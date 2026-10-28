package com.example.fifa_2026.dto;

import com.example.fifa_2026.model.Rol;

import java.util.Set;

public record GebruikerDTO(
        long id,
        Rol rol,
        String voornaam,
        String familienaam,
        String username,
        String password,
        int score,
        Set<Long> teamIds
) {}