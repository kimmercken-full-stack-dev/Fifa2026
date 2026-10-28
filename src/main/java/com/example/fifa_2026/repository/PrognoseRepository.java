package com.example.fifa_2026.repository;

import com.example.fifa_2026.model.Prognose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PrognoseRepository extends JpaRepository<Prognose, Long> {

    List<Prognose> findAllByGebruiker_IdOrderByWedstrijd_SpeeldatumAscWedstrijd_StarttijdAsc(long gebruikerId);
    void deleteByIdAndGebruiker_Id(long prognoseId, long gebruikerId);
    List<Prognose> findAllByGebruiker_Id(long gebruikerId);
    Optional<Prognose> findByGebruiker_IdAndWedstrijd_WedstrijdID(long gebruikerId, long wedstrijdId);
    List<Prognose> findAllByWedstrijd_WedstrijdID(long wedstrijdId);
    @Transactional
    void deleteAllByWedstrijd_WedstrijdID(long wedstrijdId);
}