package com.example.fifa_2026.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GebruikerRegistratieDTOValidationTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void passwordsNotEqual_violationDetected() {
        GebruikerRegistratieDTO dto = new GebruikerRegistratieDTO();
        dto.setVoornaam("Jan");
        dto.setFamilienaam("Tester");
        dto.setUsername("jantest");
        dto.setPassword("pass1");
        dto.setConfirmPassword("pass2");
        Set<ConstraintViolation<GebruikerRegistratieDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("password"))
        );    }

    @Test
    void usernamePattern_violationDetected() {
        GebruikerRegistratieDTO dto = new GebruikerRegistratieDTO();
        dto.setVoornaam("Jan");
        dto.setFamilienaam("Tester");
        dto.setUsername("jan test!"); // invalid (spatie, uitroepteken)
        dto.setPassword("pass12345");
        dto.setConfirmPassword("pass12345");
        Set<ConstraintViolation<GebruikerRegistratieDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("username"))
        );    }
    @Test
    void voornaamPattern_violationDetected() {
        GebruikerRegistratieDTO dto = new GebruikerRegistratieDTO();
        dto.setVoornaam("");
        dto.setFamilienaam("Tester");
        dto.setUsername("jan test!"); // invalid (spatie, uitroepteken)
        dto.setPassword("pass12345");
        dto.setConfirmPassword("pass12345");
        Set<ConstraintViolation<GebruikerRegistratieDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("voornaam"))
        );    }
    @Test
    void familienaamPattern_violationDetected() {
        GebruikerRegistratieDTO dto = new GebruikerRegistratieDTO();
        dto.setVoornaam("Jan");
        dto.setFamilienaam(null);
        dto.setUsername("jan test!"); // invalid (spatie, uitroepteken)
        dto.setPassword("pass12345");
        dto.setConfirmPassword("pass12345");
        Set<ConstraintViolation<GebruikerRegistratieDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("familienaam"))
        );    }
}