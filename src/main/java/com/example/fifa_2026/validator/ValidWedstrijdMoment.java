package com.example.fifa_2026.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = { WedstrijdMomentValidator.class })
@Target(TYPE)
@Retention(RUNTIME)
public @interface ValidWedstrijdMoment {

    String message() default "Ongeldige speeldatum/starttijd";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}