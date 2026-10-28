package com.example.fifa_2026.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PrognoseFormDTO {

    @NotNull
    private Long wedstrijdId;

    @NotNull @Min(0) @Max(99)
    private Integer uitslagLand1;

    @NotNull @Min(0) @Max(99)
    private Integer uitslagLand2;

}