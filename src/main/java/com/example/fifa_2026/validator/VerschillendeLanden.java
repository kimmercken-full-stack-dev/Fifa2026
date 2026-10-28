package com.example.fifa_2026.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = VerschillendeLandenValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

public @interface VerschillendeLanden {
    String message() default "Land 1 en Land 2 moeten verschillend zijn";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
