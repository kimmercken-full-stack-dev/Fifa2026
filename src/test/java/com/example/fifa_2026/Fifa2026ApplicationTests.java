package com.example.fifa_2026;

import com.example.fifa_2026.dto.TeamsDTO;
import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Rol;
import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.repository.GebruikerRepository;
import com.example.fifa_2026.repository.TeamRepository;
import com.example.fifa_2026.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class Fifa2026ApplicationTests {

    @Autowired private TeamService teamService;
    @Autowired private TeamRepository teams;
    @Autowired private GebruikerRepository gebruikers;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void uitnodigingscodeIsUniekInDeDatabase() {
        var eersteTeam = teams.saveAndFlush(new Team("Constraint-test 1"));
        var tweedeTeam = new Team("Constraint-test 2", eersteTeam.getInvitecode(), null);

        assertThat(eersteTeam.getInvitecode()).isBetween(10_000_000L, 99_999_999L);
        assertThatThrownBy(() -> teams.saveAndFlush(tweedeTeam))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void topTienWordtOpScoreEnBijGelijkeScoreOpIdGesorteerd() {
        var eigenaar = gebruikers.save(new Gebruiker(
                Rol.USER, "Top", "Tien", "top-tien-test", 100_000, "testwaarde"));
        var ids = new ArrayList<Long>();
        for (int i = 0; i < 11; i++) {
            var team = new Team("Top tien test " + i, null, eigenaar);
            eigenaar.addTeam(team);
            ids.add(teams.save(team).getTeamID());
        }

        assertThat(teamService.getTop10Teams())
                .hasSize(10)
                .allSatisfy(team -> {
                    assertThat(team.score()).isEqualTo(100_000);
                    assertThat(team.aantalLeden()).isEqualTo(1);
                })
                .extracting(TeamsDTO::teamId)
                .containsExactlyElementsOf(ids.subList(0, 10));
    }
}
