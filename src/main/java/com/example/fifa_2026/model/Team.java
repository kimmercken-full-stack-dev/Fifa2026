package com.example.fifa_2026.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(
        name = "team",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "invitecode"),
                @UniqueConstraint(columnNames = "teamNaam")
        }
)
@Getter
@Setter
@EqualsAndHashCode(of = "teamNaam")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamID;

    @Column(nullable = false, unique = true)
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
            long code = ThreadLocalRandom.current().nextLong(10_000_000L, 100_000_000L); // 8 cijfers
            this.invitecode = code;
        }
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
