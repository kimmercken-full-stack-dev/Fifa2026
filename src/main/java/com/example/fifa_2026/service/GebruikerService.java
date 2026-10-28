package com.example.fifa_2026.service;

import com.example.fifa_2026.dto.GebruikerDTO;
import com.example.fifa_2026.dto.GebruikerRegistratieDTO;
import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Rol;
import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.repository.GebruikerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GebruikerService {

    private final GebruikerRepository gebruikerRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    public Gebruiker getByIdOrThrow(long gebruikerId) {
        return gebruikerRepository.findById(gebruikerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage(
                                "error.gebruiker.nietgevonden",
                                new Object[]{gebruikerId},
                                Locale.getDefault())));
    }

    public GebruikerDTO getDtoById(long gebruikerId) {
        return toDTO(getByIdOrThrow(gebruikerId));
    }

    public List<GebruikerDTO> getAllGebruikers() {
        return gebruikerRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /** Bewaart een gebruiker met de meegegeven rol en wachtwoordwaarde; deze methode hasht het wachtwoord niet zelf. */
    @Transactional
    public Gebruiker createGebruiker(
            Rol rol,
            String voornaam,
            String familienaam,
            String username,
            String password
    ) {
        Gebruiker g = new Gebruiker(
                rol,
                voornaam,
                familienaam,
                username,
                0,
                password);

        return gebruikerRepository.save(g);
    }

    /**
     * Registratie met BCrypt-wachtwoordhash:
     * maakt een nieuwe gebruiker met rol USER en score 0.
     */
    @Transactional
    public Gebruiker registreer(GebruikerRegistratieDTO form) {

        String username = form.getUsername().trim();

        if (gebruikerRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    messageSource.getMessage(
                            "error.username.bestaat",
                            new Object[]{username},
                            Locale.getDefault()));
        }

        Gebruiker g = new Gebruiker(
                Rol.USER,
                form.getVoornaam().trim(),
                form.getFamilienaam().trim(),
                username,
                0,
                passwordEncoder.encode(form.getPassword()));

        return gebruikerRepository.save(g);
    }

    @Transactional
    public void updateScore(long gebruikerId, int nieuweScore) {
        Gebruiker g = getByIdOrThrow(gebruikerId);
        g.setScore(nieuweScore);
        gebruikerRepository.save(g);
    }

    /** Zet de gebruiker om naar een DTO met team-ID's en vult het wachtwoordveld bewust met null. */
    private GebruikerDTO toDTO(Gebruiker g) {

        Set<Long> teamIds = (g.getTeams() == null)
                ? Set.of()
                : g.getTeams()
                .stream()
                .map(Team::getTeamID)
                .collect(Collectors.toSet());

        return new GebruikerDTO(
                g.getId(),
                g.getRol(),
                g.getVoornaam(),
                g.getFamilienaam(),
                g.getUsername(),
                null,
                g.getScore(),
                teamIds);
    }
}
