package com.example.fifa_2026.validatie;

import com.example.fifa_2026.dto.WedstrijdCreateDTO;
import com.example.fifa_2026.init.WedstrijdBuilderDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
class VerschillendeLandenValidatorTest {

    @Autowired
    private Validator validator;


    @Test
    void testVerschillendeLanden() {
        WedstrijdCreateDTO dto = WedstrijdBuilderDTO.builder()
                .land1("Nederland")
                .land2("Duitsland")
                .build()
                .toDTO();

        Set<ConstraintViolation<WedstrijdCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void testZelfdeLanden() {
        WedstrijdCreateDTO dto = WedstrijdBuilderDTO.builder()
                .land1("Nederland")
                .land2("Nederland")
                .build()
                .toDTO();

        Set<ConstraintViolation<WedstrijdCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();

        assertThat(violations.stream().anyMatch(v ->
                                v.getPropertyPath().toString().equals("land2"))).isTrue();
    }
}