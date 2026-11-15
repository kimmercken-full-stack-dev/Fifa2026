package com.example.fifa_2026.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record MijnTeamsoverzichtDTO(
        long teamId,
        String teamNaam,
        String eigenaarNickname,
        int teamScore,
        List<TeamScoreboardLidDTO> leden,
        @JsonInclude(JsonInclude.Include.NON_NULL) Boolean eigenaar,
        @JsonInclude(JsonInclude.Include.NON_NULL) Long invitecode
) {
    public MijnTeamsoverzichtDTO(long teamId, String teamNaam, String eigenaarNickname,
                                 int teamScore, List<TeamScoreboardLidDTO> leden) {
        this(teamId, teamNaam, eigenaarNickname, teamScore, leden, null, null);
    }
}
