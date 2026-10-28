package com.example.fifa_2026.dto;

import java.util.List;

public record MijnTeamsoverzichtDTO(
        long teamId,
        String teamNaam,
        String eigenaarNickname,
        int teamScore,
        List<TeamScoreboardLidDTO> leden
) {}