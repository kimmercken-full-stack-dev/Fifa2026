package com.example.fifa_2026.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamCreateDTOValidationTest {

    static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void teamNaamNotBlank_violationDetected() {
        TeamCreateDTO dto = new TeamCreateDTO();
        dto.setTeamNaam(""); // leeg teamnaam
        Set<ConstraintViolation<TeamCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("teamNaam"))
        );
    }

    @Test
    void teamNaamSize_violationDetected() {
        TeamCreateDTO dto = new TeamCreateDTO();
        dto.setTeamNaam("A"); // te kort teamnaam
        Set<ConstraintViolation<TeamCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("teamNaam"))
        );    }
}
