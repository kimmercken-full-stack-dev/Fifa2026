package com.example.fifa_2026.repository;

import com.example.fifa_2026.model.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = {"teamleden", "eigenaar"})
    Optional<Team> findWithTeamledenByTeamID(Long teamID);
    Optional<Team> findByInvitecode(Long invitecode);
    boolean existsByInvitecode(Long invitecode);
    boolean existsByTeamNaamIgnoreCase(String teamNaam);
}
