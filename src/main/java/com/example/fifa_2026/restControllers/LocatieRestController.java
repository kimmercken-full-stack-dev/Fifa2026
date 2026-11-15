package com.example.fifa_2026.restControllers;

import com.example.fifa_2026.dto.LocatieDTO;
import com.example.fifa_2026.service.LocatieService;
import com.example.fifa_2026.service.WedstrijdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LocatieRestController {
    private final LocatieService locatieService;
    private final WedstrijdService wedstrijden;

    record LocatieKeuze(long id, String stadion, String stad) {}

    @GetMapping("/api/beheer/locaties")
    public List<LocatieKeuze> locaties() {
        return wedstrijden.getAllLocaties().stream()
                .map(l -> new LocatieKeuze(l.getId(), l.getStadion(), l.getStad())).toList();
    }

    /** Geeft locatie-ID en stadioncapaciteit terug als JSON voor het opgegeven locatie-ID. */
    @GetMapping("/locaties/{id}/capaciteit")
    public LocatieDTO getById(@PathVariable Long id) {
        return locatieService.getLocatieById(id);
    }

}
