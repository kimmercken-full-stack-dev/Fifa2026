package com.example.fifa_2026.init;

import com.example.fifa_2026.dto.WedstrijdCreateDTO;
import com.example.fifa_2026.model.Locatie;
import lombok.Builder;

import java.sql.Time;
import java.time.LocalDate;

import static com.example.fifa_2026.init.InitWedstrijd.*;

@Builder(toBuilder = true)
public class WedstrijdBuilderDTO {

    @Builder.Default
    private String land1 = OK_LAND1;

    @Builder.Default
    private String land2 = OK_LAND2;

    @Builder.Default
    private LocalDate datum = LocalDate.parse(OK_SPEELDATUM);

    @Builder.Default
    private Time tijd = Time.valueOf(OK_STARTTIJD);

    @Builder.Default
    private Locatie locatie = createLocatie();

    private static Locatie createLocatie() {
        Locatie locatie = new Locatie("Brussel", "Koning Boudewijnstadion", 1234, 10000);
        locatie.setId(OK_LOCATIEID);
        return locatie;
    }

    public WedstrijdCreateDTO toDTO() {
        return new WedstrijdCreateDTO(
                land1,
                land2,
                datum.toString(),
                tijd.toString(),
                locatie.getId()
        );
    }
}