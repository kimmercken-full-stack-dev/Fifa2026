package com.example.fifa_2026.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class WedstrijdUitslagDTO {

    @NotNull @Min(0) @Max(99)
    private Integer uitslagLand1;

    @NotNull @Min(0) @Max(99)
    private Integer uitslagLand2;

    public Integer getUitslagLand1() { return uitslagLand1; }
    public void setUitslagLand1(Integer uitslagLand1) { this.uitslagLand1 = uitslagLand1; }

    public Integer getUitslagLand2() { return uitslagLand2; }
    public void setUitslagLand2(Integer uitslagLand2) { this.uitslagLand2 = uitslagLand2; }
}