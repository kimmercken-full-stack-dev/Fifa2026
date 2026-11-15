package com.example.fifa_2026.repository;

import com.example.fifa_2026.model.Team;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    interface TeamRangschikking {
        Long getTeamId();
        String getTeamNaam();
        String getEigenaar();
        Long getScore();
        Long getAantalLeden();
    }

    @Query("""
            select t.teamID as teamId, t.teamNaam as teamNaam,
                   coalesce(e.username, '-') as eigenaar,
                   coalesce(sum(l.score), 0L) as score, count(l.id) as aantalLeden
            from Team t
            left join t.eigenaar e
            left join t.teamleden l
            group by t.teamID, t.teamNaam, e.username
            order by coalesce(sum(l.score), 0L) desc, t.teamID asc
            """)
    List<TeamRangschikking> findTopTeams(Pageable pageable);

    @EntityGraph(attributePaths = {"teamleden", "eigenaar"})
    Optional<Team> findWithTeamledenByTeamID(Long teamID);
    Optional<Team> findByInvitecode(Long invitecode);
    boolean existsByTeamNaamIgnoreCase(String teamNaam);
}
