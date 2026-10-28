package com.example.fifa_2026.service;

import com.example.fifa_2026.dto.LocatieDTO;
import com.example.fifa_2026.model.Locatie;
import com.example.fifa_2026.repository.LocatieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LocatieService {

    private final LocatieRepository locatieRepository;
    private final WebClient webClient;
    private final MessageSource messageSource;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    // 1. Alle locaties als DTO
    public List<LocatieDTO> getAllLocaties() {
        return locatieRepository.findAll()
                .stream().map(this::toDTO).toList();
    }

    // 2. Eén locatie ophalen als DTO op id
    public LocatieDTO getLocatieById(Long id) {
        return locatieRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("error.locatie.nietgevonden.id", new Object[]{id}, Locale.getDefault())));
    }

    /**
     * Demonstreert WebClient door het eigen REST-endpoint aan te roepen.
     * Dit is een synchrone HTTP-aanroep met een timeout van drie seconden;
     * app.base-url moet naar deze draaiende applicatie verwijzen.
     */
    public Integer getCapaciteitViaRestApi(Long id) {
        LocatieDTO locatie = webClient.get()
                .uri(appBaseUrl + "/locaties/{id}/capaciteit", id)
                .retrieve()
                .bodyToMono(LocatieDTO.class)
                .block(Duration.ofSeconds(3));

        if (locatie == null) {
            throw new IllegalStateException(
                    messageSource.getMessage("error.locatie.nietgevonden.id", new Object[]{id}, Locale.getDefault()));
        }

        return locatie.capaciteit();
    }

    // --- Mapper ---
    private LocatieDTO toDTO(Locatie locatie) {
        return new LocatieDTO(
                locatie.getId(),
                locatie.getCapaciteit());
    }
}
