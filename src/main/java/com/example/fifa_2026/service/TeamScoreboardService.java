package com.example.fifa_2026.service;

import com.example.fifa_2026.dto.MijnTeamsoverzichtDTO;
import com.example.fifa_2026.dto.TeamScoreboardLidDTO;
import com.example.fifa_2026.dto.TeamsDTO;
import com.example.fifa_2026.exception.GeenToegangException;
import com.example.fifa_2026.exception.NietGevondenException;
import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.repository.GebruikerRepository;
import com.example.fifa_2026.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TeamScoreboardService {

    private final TeamRepository teamRepository;
    private final GebruikerRepository gebruikerRepository;
    private final MessageSource messageSource;

    /** Haalt de teams op waartoe de opgegeven gebruiker behoort. */
    @Transactional(readOnly = true)
    public List<Team> getMyTeams(long gebruikerId) {

        Gebruiker g = gebruikerRepository.findById(gebruikerId)
                .orElseThrow(() -> new NietGevondenException(msg("user.notfound", gebruikerId)));

        return g.getTeams().stream().toList();
    }

    @Transactional(readOnly = true)
    public List<TeamsDTO> getMijnTeamsOverzicht(long gebruikerId) {
        return getMyTeams(gebruikerId).stream().map(TeamService::naarTeamOverzicht).toList();
    }

    /** Controleert lidmaatschap en bouwt een klassement op score, met gebruikersnaam als tweede sortering. */
    @Transactional(readOnly = true)
    public MijnTeamsoverzichtDTO getTeamScoreboard(long teamId, long gebruikerId) {

        Team team = teamRepository.findWithTeamledenByTeamID(teamId)
                .orElseThrow(() -> new NietGevondenException(msg("team.notfound", teamId)));

        // Enkel teamleden mogen scoreboard zien
        boolean isLid = team.getTeamleden().stream().anyMatch(l -> l.getId() == gebruikerId);

        if (!isLid) {
            throw new GeenToegangException(
                    msg("team.noaccess.member"));
        }

        return maakKlassement(team, null);
    }

    static MijnTeamsoverzichtDTO maakKlassement(Team team, Boolean eigenaar) {
        Gebruiker teamEigenaar = team.getEigenaar();
        var gesorteerd = team.getTeamleden()
                .stream().sorted(Comparator.comparingInt(Gebruiker::getScore)
                        .reversed().thenComparing(Gebruiker::getUsername,
                                        String.CASE_INSENSITIVE_ORDER)).toList();

        List<TeamScoreboardLidDTO> ledenDto = java.util.stream.IntStream.range(0, gesorteerd.size())
                .mapToObj(i -> {
                    Gebruiker lid = gesorteerd.get(i);
                    Boolean lidIsEigenaar = eigenaar == null ? null
                            : teamEigenaar != null && teamEigenaar.getId() == lid.getId();
                    return new TeamScoreboardLidDTO(
                            i + 1, lid.getId(), lid.getUsername(), lid.getScore(), lidIsEigenaar);
                }).toList();

        return new MijnTeamsoverzichtDTO(
                team.getTeamID(),
                team.getTeamNaam(),
                teamEigenaar != null ? teamEigenaar.getUsername() : "-",
                team.getScore(),
                ledenDto,
                eigenaar,
                Boolean.TRUE.equals(eigenaar) ? team.getInvitecode() : null);
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, Locale.getDefault());
    }
}
