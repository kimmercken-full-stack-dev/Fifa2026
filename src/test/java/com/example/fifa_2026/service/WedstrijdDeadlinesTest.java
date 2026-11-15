package com.example.fifa_2026.service;

import com.example.fifa_2026.dto.PrognoseFormDTO;
import com.example.fifa_2026.dto.WedstrijdUitslagDTO;
import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Wedstrijd;
import com.example.fifa_2026.repository.GebruikerRepository;
import com.example.fifa_2026.repository.LocatieRepository;
import com.example.fifa_2026.repository.PrognoseRepository;
import com.example.fifa_2026.repository.WedstrijdRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import java.sql.Time;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class WedstrijdDeadlinesTest {
    private final Clock klok = Clock.fixed(
            Instant.parse("2026-06-10T16:00:00Z"), ZoneId.of("Europe/Brussels"));
    private final WedstrijdRepository wedstrijden = mock(WedstrijdRepository.class);
    private final PrognoseRepository prognoses = mock(PrognoseRepository.class);
    private final GebruikerRepository gebruikers = mock(GebruikerRepository.class);
    private final MessageSource berichten = mock(MessageSource.class);

    @Test
    void prognoseWordtGeweigerdOpDeDeadlineVanAchttienUur() {
        var service = new PrognoseService(prognoses, wedstrijden, gebruikers, berichten, klok);
        when(wedstrijden.findById(1L)).thenReturn(Optional.of(wedstrijdOm("19:00:00")));
        when(gebruikers.findById(2L)).thenReturn(Optional.of(Gebruiker.builder().id(2L).build()));
        var form = new PrognoseFormDTO();
        form.setWedstrijdId(1L);

        assertThatThrownBy(() -> service.saveOrUpdatePrognose(form, 2L))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(prognoses);
    }

    @Test
    void uitslagWordtGeweigerdEenSecondeVoorDeDeadlineVanAchttienUur() {
        var service = new WedstrijdService(wedstrijden, mock(LocatieRepository.class),
                prognoses, gebruikers, berichten, klok);
        when(wedstrijden.findById(1L)).thenReturn(Optional.of(wedstrijdOm("16:00:01")));

        assertThatThrownBy(() -> service.updateUitslag(1L, new WedstrijdUitslagDTO()))
                .isInstanceOf(IllegalStateException.class);
        verify(wedstrijden, never()).save(any());
    }

    private Wedstrijd wedstrijdOm(String starttijd) {
        var wedstrijd = new Wedstrijd();
        wedstrijd.setSpeeldatum(LocalDate.of(2026, 6, 10));
        wedstrijd.setStarttijd(Time.valueOf(starttijd));
        return wedstrijd;
    }
}
