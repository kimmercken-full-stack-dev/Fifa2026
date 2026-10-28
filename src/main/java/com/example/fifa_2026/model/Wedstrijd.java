package com.example.fifa_2026.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "wedstrijdID")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class Wedstrijd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long wedstrijdID;
    private String land1;
    private String land2;
    private Integer uitslagLand1;
    private Integer uitslagLand2;
    private String winnaar;
    private LocalDate speeldatum;
    private Time starttijd;

    @ManyToOne(optional = false)
    private Locatie locatie;

    public Wedstrijd(String land1, String land2, Integer uitslagLand1, Integer uitslagLand2,
                     String winnaar, LocalDate speeldatum, Time starttijd, Locatie locatie) {
        this.land1 = land1;
        this.land2 = land2;
        this.uitslagLand1 = uitslagLand1;
        this.uitslagLand2 = uitslagLand2;
        this.winnaar = winnaar;
        this.speeldatum = speeldatum;
        this.starttijd = starttijd;
        this.locatie = locatie;
    }
    public Wedstrijd(String land1, String land2, LocalDate speeldatum, Time starttijd, Locatie locatie) {
        this.land1 = land1;
        this.land2 = land2;
        this.speeldatum = speeldatum;
        this.starttijd = starttijd;
        this.locatie = locatie;
    }
}