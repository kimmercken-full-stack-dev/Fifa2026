package com.example.fifa_2026.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WedstrijdCreateDTOValidationTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void land1NotBlank_violationDetected() {
        WedstrijdCreateDTO dto = new WedstrijdCreateDTO();
        dto.setLand1(""); // leeg land1
        Set<ConstraintViolation<WedstrijdCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("land1"))
        );
    }

    @Test
    void land2NotBlank_violationDetected() {
        WedstrijdCreateDTO dto = new WedstrijdCreateDTO();
        dto.setLand2(""); // leeg land2
        Set<ConstraintViolation<WedstrijdCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("land2"))
        );
    }

    @Test
    void speeldatumNotBlank_violationDetected() {
        WedstrijdCreateDTO dto = new WedstrijdCreateDTO();
        dto.setSpeeldatum(""); // leeg speeldatum
        Set<ConstraintViolation<WedstrijdCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("speeldatum"))
        );
    }

    @Test
    void starttijdNotBlank_violationDetected() {
        WedstrijdCreateDTO dto = new WedstrijdCreateDTO();
        dto.setStarttijd(""); // leeg starttijd
        Set<ConstraintViolation<WedstrijdCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("starttijd"))
        );
    }

    @Test
    void locatieIdPositive_violationDetected() {
        WedstrijdCreateDTO dto = new WedstrijdCreateDTO();
        dto.setLocatieId(-1L); // negatieve locatieId
        Set<ConstraintViolation<WedstrijdCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("locatieId"))
        );
    }
}
