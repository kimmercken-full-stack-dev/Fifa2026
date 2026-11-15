package com.example.fifa_2026.restControllers;

import com.example.fifa_2026.dto.GebruikerRegistratieDTO;
import com.example.fifa_2026.model.Rol;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.GebruikerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class GebruikerRestController {
    private final CurrentUserService currentUser;
    private final GebruikerService gebruikers;

    record Profiel(String username, String voornaam, String familienaam, int score, boolean admin, Rol rol) {}

    @GetMapping("/profiel")
    public Profiel profiel() {
        var gebruiker = currentUser.getCurrentGebruikerOrThrow();
        boolean admin = currentUser.isAdmin();
        return new Profiel(gebruiker.getUsername(), gebruiker.getVoornaam(), gebruiker.getFamilienaam(),
                gebruiker.getScore(), admin, admin ? Rol.ADMIN : Rol.USER);
    }

    @PostMapping("/registreer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registreer(@Valid @RequestBody GebruikerRegistratieDTO form) {
        gebruikers.registreer(form);
    }
}
