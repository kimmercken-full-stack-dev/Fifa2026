package com.example.fifa_2026.service;

import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.repository.GebruikerRepository;
import com.example.fifa_2026.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class TeamServiceTest {
    @Test
    void botsingBijOpslaanWordtTeruggedraaidEnOpnieuwGeprobeerd() {
        var teams = mock(TeamRepository.class);
        var gebruikers = mock(GebruikerRepository.class);
        var manager = mock(PlatformTransactionManager.class);
        var service = new TeamService(teams, gebruikers, mock(MessageSource.class), manager);
        var eersteTransactie = new SimpleTransactionStatus();
        var tweedeTransactie = new SimpleTransactionStatus();
        when(manager.getTransaction(argThat(definitie ->
                definitie.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW)))
                .thenReturn(eersteTransactie, tweedeTransactie);

        var eigenaar = Gebruiker.builder().id(1L).build();
        var team = new Team("Supporters", 11111111L, eigenaar);
        when(teams.findById(3L)).thenReturn(Optional.of(team));
        when(teams.saveAndFlush(any(Team.class)))
                .thenThrow(new DataIntegrityViolationException("Dubbele code",
                        new SQLException("Duplicate entry", "23000", 1062)))
                .thenReturn(team);

        try (var codes = mockStatic(Team.class)) {
            codes.when(Team::nieuweUitnodigingscode).thenReturn(22222222L, 33333333L);

            assertThat(service.regenerateInvitecode(3L, 1L)).isEqualTo(33333333L);
        }

        var volgorde = inOrder(manager, teams);
        volgorde.verify(teams).saveAndFlush(team);
        volgorde.verify(manager).rollback(eersteTransactie);
        volgorde.verify(teams).saveAndFlush(team);
        volgorde.verify(manager).commit(tweedeTransactie);
    }
}
