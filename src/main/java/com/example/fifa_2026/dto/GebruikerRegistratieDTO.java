package com.example.fifa_2026.dto;

import com.example.fifa_2026.validator.ValidPasswords;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ValidPasswords
public class GebruikerRegistratieDTO {

    @NotBlank
    @NotNull
    @Size(min = 2, max = 50)
    private String voornaam;

    @NotBlank
    @NotNull
    @Size(min = 2, max = 50)
    private String familienaam;

    @NotBlank
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "username must be alphanumeric with no spaces")
    @Size(min = 4, max = 15)
    private String username;

    @NotBlank
    @NotNull
    @Size(min = 8, max = 72)
    private String password;

    @NotBlank
    private String confirmPassword;
}