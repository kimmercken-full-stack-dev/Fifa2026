package com.example.fifa_2026.service;

import com.example.fifa_2026.dto.TeamsDTO;
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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Beheert teams, uitnodigingen en lidmaatschap.
 * Eigenaarsrechten worden in deze laag gecontroleerd. Bij lidmaatschapswijzigingen
 * worden beide kanten van de JPA-relatie binnen dezelfde transactie bijgewerkt.
 */
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final GebruikerRepository gebruikerRepository;
    private final MessageSource messageSource;

    /** Sorteert teams op de som van hun ledenscores en geeft maximaal tien overzichts-DTO's terug. */
    public List<TeamsDTO> getTop10Teams() {
        return teamRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Team::getScore).reversed())
                .limit(10)
                .map(t -> new TeamsDTO(
                        t.getTeamID(),
                        t.getTeamNaam(),
                        t.getEigenaar() != null ? t.getEigenaar().getUsername() : "-",
                        t.getScore(),
                        t.getTeamleden().size()
                )).toList();
    }

    /** Haalt het team met leden op en weigert toegang wanneer de gebruiker geen lid is. */
    @Transactional(readOnly = true)
    public Team getTeamForViewOrThrow(long teamId, long currentUserId) {
        Team team = teamRepository.findWithTeamledenByTeamID(teamId)
                .orElseThrow(() -> new IllegalArgumentException(
                        msg("team.notfound", teamId)));

        boolean isLid = team.getTeamleden().stream().anyMatch(g -> g.getId() == currentUserId);

        if (!isLid) {
            throw new IllegalStateException(msg("team.noaccess.member"));
        }
        return team;
    }

    private boolean isOwner(Team team, long currentUserId) {
        return team.getEigenaar() != null && team.getEigenaar().getId() == currentUserId;
    }

    /** Controleert eigenaarschap, beschermt de eigenaar tegen verwijdering en werkt beide kanten van het lidmaatschap bij. */
    @Transactional
    public void removeMember(long teamId, long memberUserId, long currentUserId) {

        Team team = teamRepository.findWithTeamledenByTeamID(teamId)
                .orElseThrow(() -> new IllegalArgumentException(msg("team.notfound", teamId)));

        // enkel owner mag verwijderen
        if (!isOwner(team, currentUserId)) {
            throw new IllegalStateException(msg("team.noaccess.owner"));
        }

        // eigenaar niet verwijderen
        if (team.getEigenaar() != null && team.getEigenaar().getId() == memberUserId) {
            throw new IllegalStateException(msg("team.owner.cannotremove"));
        }

        Gebruiker lid = gebruikerRepository.findById(memberUserId)
                .orElseThrow(() -> new IllegalArgumentException(msg("user.notfound", memberUserId)));

        // beide kanten bijwerken
        team.getTeamleden().remove(lid);
        lid.getTeams().remove(team);

        teamRepository.save(team);
        gebruikerRepository.save(lid);
    }

    /** Vervangt de uitnodigingscode nadat is gecontroleerd dat de aanvrager eigenaar is. */
    @Transactional
    public Long regenerateInvitecode(long teamId, long currentUserId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException(msg("team.notfound", teamId)));

        if (!isOwner(team, currentUserId)) {
            throw new IllegalStateException(msg("team.noaccess.owner"));
        }

        long newCode = generateUniqueInvitecode();

        team.setInvitecode(newCode);

        teamRepository.save(team);

        return newCode;
    }

    /** Zoekt het team via de uitnodigingscode en voegt de gebruiker toe als die nog geen lid is. */
    @Transactional
    public Team joinTeamByInvitecode(long invitecode, long currentUserId) {

        Team team = teamRepository.findByInvitecode(invitecode)
                .orElseThrow(() -> new IllegalArgumentException(msg("team.invite.invalid")));

        Gebruiker user = gebruikerRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException(msg("user.notfound", currentUserId)));

        // al lid? dan niets doen
        boolean alreadyMember = team.getTeamleden().stream().anyMatch(g -> g.getId() == currentUserId);

        if (!alreadyMember) {
            team.getTeamleden().add(user);
            user.getTeams().add(team);

            teamRepository.save(team);
            gebruikerRepository.save(user);
        }

        return team;
    }

    /** Controleert de teamnaam, genereert een code en maakt de aanvrager zowel eigenaar als lid. */
    @Transactional
    public Team createTeam(String teamNaam, long currentUserId) {

        String naam = teamNaam.trim();

        if (teamRepository.existsByTeamNaamIgnoreCase(naam)) {
            throw new IllegalArgumentException(msg("team.name.exists", naam));
        }

        Gebruiker eigenaar = gebruikerRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException(msg("user.notfound", currentUserId)));

        Team team = new Team(naam, generateUniqueInvitecode(), eigenaar);

        team.setEigenaar(eigenaar);

        // eigenaar is ook teamlid
        team.getTeamleden().add(eigenaar);
        eigenaar.getTeams().add(team);

        Team saved = teamRepository.save(team);

        gebruikerRepository.save(eigenaar);

        return saved;
    }
    /** Controleert eigenaarschap, verwijdert de lidmaatschapskoppelingen en verwijdert daarna het team. */
    @Transactional
    public void deleteTeam(long teamId, long currentUserId) {

        Team team = teamRepository.findWithTeamledenByTeamID(teamId)
                .orElseThrow(() -> new IllegalArgumentException(msg("team.notfound", teamId)));

        // enkel eigenaar mag verwijderen
        if (!isOwner(team, currentUserId)) {
            throw new IllegalStateException(msg("team.noaccess.owner"));
        }

        // relatie verwijderen bij alle leden
        for (Gebruiker gebruiker : team.getTeamleden()) {
            gebruiker.getTeams().remove(team);
        }

        team.getTeamleden().clear();

        teamRepository.delete(team);
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, Locale.getDefault()
        );
    }

    /** Genereert een achtcijferige code en probeert opnieuw zolang die al in de database voorkomt. */
    private long generateUniqueInvitecode() {
        long code;
        do {
            code = ThreadLocalRandom.current().nextLong(10_000_000L, 100_000_000L);
        } while (teamRepository.existsByInvitecode(code));
        return code;
    }
}
