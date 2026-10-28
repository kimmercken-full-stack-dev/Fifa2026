package com.example.fifa_2026.validatie;

import com.example.fifa_2026.dto.WedstrijdCreateDTO;
import com.example.fifa_2026.init.WedstrijdBuilderDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WedstrijdMomentValidatorTest {

    @Autowired
    private Validator validator;

    @Test
    void testGeldigWedstrijdMoment() {

        WedstrijdCreateDTO dto = WedstrijdBuilderDTO.builder()
                .datum(LocalDate.now().plusDays(5))
                .tijd(java.sql.Time.valueOf("15:00:00"))
                .build()
                .toDTO();

        Set<ConstraintViolation<WedstrijdCreateDTO>> violations =
                validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void testSpeeldatumVoorStartDatum() {

        WedstrijdCreateDTO dto = WedstrijdBuilderDTO.builder()
                .datum(LocalDate.of(2025, 1, 1))
                .tijd(java.sql.Time.valueOf("15:00:00"))
                .build()
                .toDTO();

        Set<ConstraintViolation<WedstrijdCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();

        assertThat(
                violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("speeldatum"))).isTrue();
    }

    @Test
    void testWedstrijdMomentInVerleden() {
        LocalDate gisteren = LocalDate.now().minusDays(1);
        WedstrijdCreateDTO dto = WedstrijdBuilderDTO.builder()
                .datum(gisteren)
                .tijd(java.sql.Time.valueOf("15:00:00"))
                .build()
                .toDTO();

        Set<ConstraintViolation<WedstrijdCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();

        assertThat(
                violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("speeldatum"))).isTrue();
    }

    @Test
    void testVandaagMaarMinderDan2UurOpVoorhand() {
        LocalDate vandaag = LocalDate.now();
        LocalTime binnenEenUur = LocalTime.now().plusHours(1);
        WedstrijdCreateDTO dto = WedstrijdBuilderDTO.builder()
                .datum(vandaag)
                .tijd(java.sql.Time.valueOf(binnenEenUur))
                .build()
                .toDTO();

        Set<ConstraintViolation<WedstrijdCreateDTO>> violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();

        assertThat(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("starttijd"))).isTrue();
    }
}