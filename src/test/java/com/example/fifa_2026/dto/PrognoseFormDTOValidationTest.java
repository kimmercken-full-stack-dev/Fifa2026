package com.example.fifa_2026.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PrognoseFormDTOValidationTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void scoreLand1NotNegative_violationDetected() {
        PrognoseFormDTO dto = new PrognoseFormDTO();
        dto.setWedstrijdId(7L);
        dto.setUitslagLand1(-1);
        dto.setUitslagLand2(2);
        Set<ConstraintViolation<PrognoseFormDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand1"))
        );
    }
    @Test
    void scoreLand2NotNegative_violationDetected() {
        PrognoseFormDTO dto = new PrognoseFormDTO();
        dto.setWedstrijdId(7L);
        dto.setUitslagLand1(1);
        dto.setUitslagLand2(-2);
        Set<ConstraintViolation<PrognoseFormDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand2"))
        );
    }
    @Test
    void scoreLand1TeHoog_violationDetected() {
        PrognoseFormDTO dto = new PrognoseFormDTO();
        dto.setWedstrijdId(7L);
        dto.setUitslagLand1(1001);
        dto.setUitslagLand2(1);
        Set<ConstraintViolation<PrognoseFormDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand1"))
        );    }
    @Test
    void scoreLand2TeHoog_violationDetected() {
        PrognoseFormDTO dto = new PrognoseFormDTO();
        dto.setWedstrijdId(7L);
        dto.setUitslagLand1(1);
        dto.setUitslagLand2(1001);
        Set<ConstraintViolation<PrognoseFormDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand2"))
        );    }
    @Test
    void wedstrijdIdNotNull_violationDetected() {
        PrognoseFormDTO dto = new PrognoseFormDTO();
        dto.setWedstrijdId(null);
        dto.setUitslagLand1(1);
        dto.setUitslagLand2(0);
        Set<ConstraintViolation<PrognoseFormDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("wedstrijdId"))
        );    }
    @Test
    void uitslagLand1NotNull_violationDetected() {
        PrognoseFormDTO dto = new PrognoseFormDTO();
        dto.setWedstrijdId(5L);
        dto.setUitslagLand1(null);
        dto.setUitslagLand2(0);
        Set<ConstraintViolation<PrognoseFormDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand1"))
        );    }
    @Test
    void uitslagLand2NotNull_violationDetected() {
        PrognoseFormDTO dto = new PrognoseFormDTO();
        dto.setWedstrijdId(7L);
        dto.setUitslagLand1(2);
        dto.setUitslagLand2(null);
        Set<ConstraintViolation<PrognoseFormDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("uitslagLand2"))
        );    }

}
