package com.example.fifa_2026.dto;

import com.example.fifa_2026.model.Locatie;
import com.example.fifa_2026.validator.ValidWedstrijdMoment;
import com.example.fifa_2026.validator.VerschillendeLanden;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@ValidWedstrijdMoment
@VerschillendeLanden
public class WedstrijdEditDTO {

    @NotBlank
    @NotNull
    private String land1;

    @NotBlank
    @NotNull
    private String land2;
    @NotBlank
    @NotNull
    private String speeldatum; // yyyy-MM-dd (input type=date)
    @NotBlank
    @NotNull
    private String starttijd;  // HH:mm (input type=time)
    @NotNull
    @Positive
    private Long locatieId;

    public String getLand1() { return land1; }
    public void setLand1(String land1) { this.land1 = land1; }

    public String getLand2() { return land2; }
    public void setLand2(String land2) { this.land2 = land2; }

    public String getSpeeldatum() { return speeldatum; }
    public void setSpeeldatum(String speeldatum) { this.speeldatum = speeldatum; }

    public String getStarttijd() { return starttijd; }
    public void setStarttijd(String starttijd) { this.starttijd = starttijd; }

    public Long getLocatieId() {
        return locatieId;
    }

    public void setLocatieId(Long locatieId) {
        this.locatieId = locatieId;
    }
}
