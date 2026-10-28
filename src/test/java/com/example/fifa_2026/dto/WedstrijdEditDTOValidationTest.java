package com.example.fifa_2026.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WedstrijdEditDTOValidationTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();}

    @Test
    void land1NotBlank_violationDetected() {
        WedstrijdEditDTO dto = new WedstrijdEditDTO();
        dto.setLand1("");
        Set<ConstraintViolation<WedstrijdEditDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("land1")));
    }

    @Test
    void land1NotNull_violationDetected() {
        WedstrijdEditDTO dto = new WedstrijdEditDTO();
        dto.setLand1(null);
        Set<ConstraintViolation<WedstrijdEditDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("land1")));
    }

    @Test
    void land2NotBlank_violationDetected() {

        WedstrijdEditDTO dto = new WedstrijdEditDTO();
        dto.setLand2("");
        Set<ConstraintViolation<WedstrijdEditDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("land2")));
    }

    @Test
    void land2NotNull_violationDetected() {
        WedstrijdEditDTO dto = new WedstrijdEditDTO();
        dto.setLand2(null);
        Set<ConstraintViolation<WedstrijdEditDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("land2")));
    }

    @Test
    void speeldatumNotBlank_violationDetected() {
        WedstrijdEditDTO dto = new WedstrijdEditDTO();
        dto.setSpeeldatum("");
        Set<ConstraintViolation<WedstrijdEditDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("speeldatum")));
    }

    @Test
    void speeldatumNotNull_violationDetected() {
        WedstrijdEditDTO dto = new WedstrijdEditDTO();
        dto.setSpeeldatum(null);
        Set<ConstraintViolation<WedstrijdEditDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("speeldatum")));
    }

    @Test
    void starttijdNotBlank_violationDetected() {
        WedstrijdEditDTO dto = new WedstrijdEditDTO();
        dto.setStarttijd("");
        Set<ConstraintViolation<WedstrijdEditDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("starttijd")));
    }

    @Test
    void starttijdNotNull_violationDetected() {
        WedstrijdEditDTO dto = new WedstrijdEditDTO();
        dto.setStarttijd(null);
        Set<ConstraintViolation<WedstrijdEditDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("starttijd")));
    }

    @Test
    void locatieIdNotNull_violationDetected() {
        WedstrijdEditDTO dto = new WedstrijdEditDTO();
        dto.setLocatieId(null);
        Set<ConstraintViolation<WedstrijdEditDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("locatieId")));
    }

    @Test
    void locatieIdPositive_violationDetected() {
        WedstrijdEditDTO dto = new WedstrijdEditDTO();
        dto.setLocatieId(0L);
        Set<ConstraintViolation<WedstrijdEditDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("locatieId")));
    }
}