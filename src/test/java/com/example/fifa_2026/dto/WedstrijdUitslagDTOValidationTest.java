package com.example.fifa_2026.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WedstrijdUitslagDTOValidationTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void uitslagLand1NotNull_violationDetected() {
        WedstrijdUitslagDTO dto = new WedstrijdUitslagDTO();
        dto.setUitslagLand1(null); // null uitslag
        Set<ConstraintViolation<WedstrijdUitslagDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand1"))
        );
    }

    @Test
    void uitslagLand1Min_violationDetected() {
        WedstrijdUitslagDTO dto = new WedstrijdUitslagDTO();
        dto.setUitslagLand1(-1); // negatieve uitslag
        Set<ConstraintViolation<WedstrijdUitslagDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand1"))
        );
    }

    @Test
    void uitslagLand1Max_violationDetected() {
        WedstrijdUitslagDTO dto = new WedstrijdUitslagDTO();
        dto.setUitslagLand1(100); // te hoge uitslag
        Set<ConstraintViolation<WedstrijdUitslagDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand1"))
        );
    }

    @Test
    void uitslagLand2NotNull_violationDetected() {
        WedstrijdUitslagDTO dto = new WedstrijdUitslagDTO();
        dto.setUitslagLand2(null); // null uitslag
        Set<ConstraintViolation<WedstrijdUitslagDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand2"))
        );
    }

    @Test
    void uitslagLand2Min_violationDetected() {
        WedstrijdUitslagDTO dto = new WedstrijdUitslagDTO();
        dto.setUitslagLand2(-1); // negatieve uitslag
        Set<ConstraintViolation<WedstrijdUitslagDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand2"))
        );
    }

    @Test
    void uitslagLand2Max_violationDetected() {
        WedstrijdUitslagDTO dto = new WedstrijdUitslagDTO();
        dto.setUitslagLand2(100); // te hoge uitslag
        Set<ConstraintViolation<WedstrijdUitslagDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand2"))
        );
    }
}
