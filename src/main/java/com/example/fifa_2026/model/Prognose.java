package com.example.fifa_2026.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor

public class Prognose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private long id;

    @Setter
    private Integer uitslagLand1;
    @Setter
    private Integer uitslagLand2;
    @Setter
    private String winnaar;
    @ManyToOne
    @Setter private Wedstrijd wedstrijd;
    @ManyToOne
    @Setter private Gebruiker gebruiker;


    public Prognose(int id, int uitslagLand1, int uitslagLand2) {
        this.uitslagLand1 =uitslagLand1;
        this.uitslagLand2 =uitslagLand2;
    }
}
