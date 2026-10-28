package com.example.fifa_2026.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        name = "locatie",
        uniqueConstraints = @UniqueConstraint(columnNames = "stadiumcode")
)
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@JsonPropertyOrder({"locatie_id", "capaciteit"})
public class Locatie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("locatie_id")
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String stad;

    @Column(nullable = false)
    @NotBlank
    private String stadion;

    @Column(nullable = false, unique = true)
    @NotNull
    @Min(1000)
    @Max(9999)
    private Integer stadiumcode; // 4 cijfers, uniek

    @Column(nullable = false)
    @NotNull
    @Min(0)
    @Max(96)
    private Integer checksum; // stadiumcode % 97

    @Column(nullable = false)
    @NotNull
    @Min(1000)
    private Integer capaciteit; // maximum aantal bezoekers in het stadion

    public Locatie(String stad, String stadion, int stadiumcode, int capaciteit) {
        this.stad = stad;
        this.stadion = stadion;
        this.stadiumcode = stadiumcode;
        this.capaciteit = capaciteit;
    }
    public Locatie( Long id,Integer capaciteit) {
this.id=id;
        this.capaciteit = capaciteit;
    }

    /** Controleert de viercijferige stadioncode en berekent of verifieert de checksum voordat JPA opslaat. */
    @PrePersist
    @PreUpdate
    private void validateAndComputeChecksum() {
        if (stadiumcode == null || stadiumcode < 1000 || stadiumcode > 9999) {
            throw new IllegalArgumentException("stadiumcode moet exact 4 cijfers zijn (1000-9999).");
        }
        int expectedChecksum = stadiumcode % 97;
        if (checksum != null && !checksum.equals(expectedChecksum)) {
            throw new IllegalArgumentException("checksum moet gelijk zijn aan stadiumcode % 97.");
        }
        this.checksum = expectedChecksum;
    }
}
