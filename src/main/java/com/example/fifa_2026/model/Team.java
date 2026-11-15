package com.example.fifa_2026.model;

import jakarta.persistence.*;
import lombok.*;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "team",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_team_invitecode", columnNames = "invitecode"),
                @UniqueConstraint(columnNames = "teamNaam")
        }
)
@Getter
@Setter
@EqualsAndHashCode(of = "teamNaam")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Team {
    private static final SecureRandom UITNODIGING_RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamID;

    @Column(nullable = false)
    private Long invitecode;

    @Column(nullable = false, unique = true)
    private String teamNaam;

    @ManyToOne
    private Gebruiker eigenaar;

    @ManyToMany
    private Set<Gebruiker> teamleden = new HashSet<>();

    // Constructor “met alle velden die je zelf wil zetten” (zonder ID)
    public Team(String teamNaam,Long invitecode, Gebruiker eigenaar) {
        this.teamNaam = teamNaam;
        this.invitecode = invitecode;
        this.eigenaar = eigenaar;
    }

    public Team(String teamNaam) {
        this.teamNaam = teamNaam;
    }

    /** Vult bij de eerste opslag een ontbrekende uitnodigingscode aan met acht willekeurige cijfers. */
    @PrePersist
    private void generateInvitecodeIfMissing() {
        if (this.invitecode == null) {
            this.invitecode = nieuweUitnodigingscode();
        }
    }

    public static long nieuweUitnodigingscode() {
        return UITNODIGING_RANDOM.nextInt(10_000_000, 100_000_000);
    }

    /** Som van de persoonlijke scores van alle teamleden */
    @Transient
    public int getScore() {
        return teamleden.stream()
                .mapToInt(Gebruiker::getScore)
                .sum();
    }

    /** Verwijdert het lidmaatschap aan zowel de teamkant als de gebruikerskant. */
    public void removeLid(Gebruiker gebruiker) {
        this.teamleden.remove(gebruiker);
        gebruiker.getTeams().remove(this);
    }

    @Override
    public String toString() {
        return String.format("%s %s", teamNaam, eigenaar);
    }
}
