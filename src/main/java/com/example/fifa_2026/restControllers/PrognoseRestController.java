package com.example.fifa_2026.restControllers;

import com.example.fifa_2026.dto.PrognoseFormDTO;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.PrognoseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PrognoseRestController {
    private final CurrentUserService currentUser;
    private final PrognoseService prognoses;

    @GetMapping("/prognoses")
    public List<PrognoseFormDTO> eigenPrognoses() {
        return prognoses.getPrognoseOverzicht(gebruikerId());
    }

    @GetMapping("/prognoses/{id}")
    public PrognoseFormDTO prognose(@PathVariable long id) {
        return prognoses.getPrognoseFormForWedstrijdAndGebruiker(id, gebruikerId());
    }

    @PutMapping("/prognoses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bewaarPrognose(@PathVariable long id, @Valid @RequestBody PrognoseFormDTO form) {
        form.setWedstrijdId(id);
        prognoses.saveOrUpdatePrognose(form, gebruikerId());
    }

    @DeleteMapping("/prognoses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verwijderPrognose(@PathVariable long id) {
        prognoses.deletePrognoseById(id, gebruikerId());
    }

    private long gebruikerId() {
        return currentUser.getCurrentGebruikerIdOrThrow();
    }
}
