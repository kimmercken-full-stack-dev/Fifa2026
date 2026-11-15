package com.example.fifa_2026.restControllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SessieRestController {
    private final LocaleResolver localeResolver;

    record Taalkeuze(@NotNull @Pattern(regexp = "nl|en") String taal) {}

    @PutMapping("/taal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void wijzigTaal(@Valid @RequestBody Taalkeuze keuze,
                           HttpServletRequest request, HttpServletResponse response) {
        localeResolver.setLocale(request, response, Locale.forLanguageTag(keuze.taal()));
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
