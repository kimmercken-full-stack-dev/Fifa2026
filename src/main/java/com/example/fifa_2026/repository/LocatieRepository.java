package com.example.fifa_2026.repository;

import com.example.fifa_2026.model.Locatie;
import com.example.fifa_2026.model.Wedstrijd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocatieRepository extends JpaRepository<Locatie, Long> {
    List<Locatie> findAll();
    Optional<Locatie> findById(Long id);
}