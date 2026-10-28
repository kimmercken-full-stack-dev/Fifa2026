package com.example.fifa_2026.repository;

import com.example.fifa_2026.model.Gebruiker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GebruikerRepository extends JpaRepository<Gebruiker, Long> {
    boolean existsByUsername(String username);
    Optional<Gebruiker> findByUsername(String username);
}