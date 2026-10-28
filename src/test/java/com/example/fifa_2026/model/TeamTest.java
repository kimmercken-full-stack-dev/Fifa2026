package com.example.fifa_2026.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamTest {

    @Test
    void prePersist_generatesEightDigitInvitecodeWhenMissing() throws Exception {
        Team team = new Team("Testteam");

        invokePrePersist(team);

        assertNotNull(team.getInvitecode());
        assertTrue(team.getInvitecode() >= 10_000_000L);
        assertTrue(team.getInvitecode() < 100_000_000L);
    }

    @Test
    void getScore_returnsSumOfMemberScores() {
        Team team = new Team("Scoreteam");
        Gebruiker an = new Gebruiker(Rol.USER, "An", "Aarts", "an", 4, "pw");
        an.setId(1L);
        Gebruiker bo = new Gebruiker(Rol.USER, "Bo", "Boon", "bo", 7, "pw");
        bo.setId(2L);

        team.getTeamleden().add(an);
        team.getTeamleden().add(bo);

        assertEquals(11, team.getScore());
    }

    private void invokePrePersist(Team team) throws Exception {
        Method method = Team.class.getDeclaredMethod("generateInvitecodeIfMissing");
        method.setAccessible(true);
        method.invoke(team);
    }
}
