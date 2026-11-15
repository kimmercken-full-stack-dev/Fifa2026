package com.example.fifa_2026.service;

import com.example.fifa_2026.dto.TeamsDTO;
import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.repository.GebruikerRepository;
import com.example.fifa_2026.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.function.LongFunction;

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
    private final PlatformTransactionManager transactionManager;

    /** Sorteert teams op de som van hun ledenscores en geeft maximaal tien overzichts-DTO's terug. */
    public List<TeamsDTO> getTop10Teams() {
        return teamRepository.findTopTeams(PageRequest.of(0, 10)).stream()
                .map(t -> new TeamsDTO(
                        t.getTeamId(),
                        t.getTeamNaam(),
                        t.getEigenaar(),
                        Math.toIntExact(t.getScore()),
                        Math.toIntExact(t.getAantalLeden())
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
    public Long regenerateInvitecode(long teamId, long currentUserId) {
        return metNieuweUitnodigingscode(code -> vernieuwUitnodigingscode(teamId, currentUserId, code));
    }

    private Long vernieuwUitnodigingscode(long teamId, long currentUserId, long newCode) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException(msg("team.notfound", teamId)));

        if (!isOwner(team, currentUserId)) {
            throw new IllegalStateException(msg("team.noaccess.owner"));
        }

        if (team.getInvitecode() == newCode) {
            return null;
        }

        team.setInvitecode(newCode);
        teamRepository.saveAndFlush(team);

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
    public Team createTeam(String teamNaam, long currentUserId) {
        return metNieuweUitnodigingscode(code -> maakTeam(teamNaam.trim(), currentUserId, code));
    }

    private Team maakTeam(String naam, long currentUserId, long code) {
        if (teamRepository.existsByTeamNaamIgnoreCase(naam)) {
            throw new IllegalArgumentException(msg("team.name.exists", naam));
        }

        Gebruiker eigenaar = gebruikerRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException(msg("user.notfound", currentUserId)));

        Team team = new Team(naam, code, eigenaar);

        team.setEigenaar(eigenaar);

        // eigenaar is ook teamlid
        team.getTeamleden().add(eigenaar);
        eigenaar.getTeams().add(team);

        Team saved = teamRepository.saveAndFlush(team);

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

    private <T> T metNieuweUitnodigingscode(LongFunction<T> actie) {
        var transactie = new TransactionTemplate(transactionManager);
        transactie.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        for (int poging = 0; poging < 5; poging++) {
            long code = Team.nieuweUitnodigingscode();
            try {
                T resultaat = transactie.execute(status -> actie.apply(code));
                if (resultaat != null) {
                    return resultaat;
                }
            } catch (DataIntegrityViolationException ex) {
                // De mislukte transactie is teruggedraaid voordat we een nieuwe code proberen.
                if (!(ex.getMostSpecificCause() instanceof SQLException sql)
                        || sql.getErrorCode() != 1062) {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException(msg("team.invite.generation.failed"));
    }
}
