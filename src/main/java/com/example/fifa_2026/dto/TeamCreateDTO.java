package com.example.fifa_2026.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TeamCreateDTO {

    @NotBlank(message = "Teamnaam is verplicht.")
    @Size(min = 2, max = 50, message = "Teamnaam moet tussen 2 en 50 tekens zijn.")
    private String teamNaam;

}