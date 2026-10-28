package com.example.fifa_2026.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Gebruiker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //@Getter(AccessLevel.NONE)
    private long id;

    private Rol rol;
    private String voornaam;
    private String familienaam;

    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;

    private int score;

    @ManyToMany(mappedBy = "teamleden")
    private Set<Team> teams = new HashSet<>();

    public Gebruiker(Rol rol, String voornaam, String familienaam, String username, int score, String password) {
        this.rol = rol;
        this.voornaam = voornaam;
        this.familienaam = familienaam;
        this.username = username;
        this.score = score;
        this.password = password;
    }

    public void addTeam(Team team) {
        teams.add(team);
        team.getTeamleden().add(this); // Zorg ervoor dat de gebruiker ook aan het team wordt toegevoegd
    }
    public void removeTeam(Team team) {
        this.teams.remove(team);
        team.getTeamleden().remove(this);
    }

}
