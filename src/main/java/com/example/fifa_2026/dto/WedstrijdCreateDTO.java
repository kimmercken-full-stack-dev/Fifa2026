package com.example.fifa_2026.dto;

import com.example.fifa_2026.validator.ValidWedstrijdMoment;
import com.example.fifa_2026.validator.VerschillendeLanden;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ValidWedstrijdMoment
@VerschillendeLanden
public class WedstrijdCreateDTO {

    @NotBlank
    @NotNull
    private String land1;

    @NotBlank
    @NotNull
    private String land2;
    @NotBlank
    @NotNull
    private String speeldatum; // yyyy-MM-dd (input type=date)
    @NotBlank
    @NotNull
    private String starttijd;  // HH:mm (input type=time)

    @NotNull
    @Positive
    private Long locatieId;

}
