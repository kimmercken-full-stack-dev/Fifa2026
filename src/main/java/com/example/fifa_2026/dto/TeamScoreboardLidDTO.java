package com.example.fifa_2026.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public record TeamScoreboardLidDTO(
        int positie,
        long gebruikerId,
        String nickname,
        int score,
        @JsonInclude(JsonInclude.Include.NON_NULL) Boolean eigenaar
) {
    public TeamScoreboardLidDTO(int positie, long gebruikerId, String nickname, int score) {
        this(positie, gebruikerId, nickname, score, null);
    }
}
