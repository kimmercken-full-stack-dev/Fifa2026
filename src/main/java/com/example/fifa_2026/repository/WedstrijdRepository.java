package com.example.fifa_2026.repository;

import com.example.fifa_2026.model.Locatie;
import com.example.fifa_2026.model.Team;
import com.example.fifa_2026.model.Wedstrijd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

public interface WedstrijdRepository  extends JpaRepository<Wedstrijd, Long> {
    List<Wedstrijd> findAllByOrderBySpeeldatumAscStarttijdAsc();
    List<Wedstrijd> findBySpeeldatumAndLocatie(LocalDate speeldatum, Locatie locatie);
    List<Wedstrijd> findBySpeeldatumOrderByStarttijdAsc(LocalDate speeldatum);

}
