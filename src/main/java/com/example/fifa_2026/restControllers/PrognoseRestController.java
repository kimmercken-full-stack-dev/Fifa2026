package com.example.fifa_2026.restControllers;

import com.example.fifa_2026.dto.PrognoseFormDTO;
import com.example.fifa_2026.service.CurrentUserService;
import com.example.fifa_2026.service.PrognoseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PrognoseRestController {
    private final CurrentUserService currentUser;
    private final PrognoseService prognoses;

    record PrognoseItem(long id, long wedstrijdId, String land1, String land2,
                        String speeldatum, String starttijd, String stad, String stadion, String winnaar,
                        Integer uitslagLand1, Integer uitslagLand2, boolean wijzigbaar) {}

    @GetMapping("/prognoses")
    public List<PrognoseItem> eigenPrognoses() {
        var now = LocalDateTime.now();
        return prognoses.getPrognosesForGebruiker(gebruikerId()).stream().map(p -> {
            var w = p.getWedstrijd();
            var aftrap = w.getSpeeldatum().atTime(w.getStarttijd().toLocalTime());
            var deadline = PrognoseService.prognoseDeadline(aftrap);
            return new PrognoseItem(p.getId(), w.getWedstrijdID(), w.getLand1(), w.getLand2(),
                    w.getSpeeldatum().toString(), w.getStarttijd().toString(),
                    w.getLocatie().getStad(), w.getLocatie().getStadion(), p.getWinnaar(),
                    p.getUitslagLand1(), p.getUitslagLand2(), now.isBefore(deadline));
        }).toList();
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
