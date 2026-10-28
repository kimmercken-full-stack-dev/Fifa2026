package com.example.fifa_2026.validator;

import com.example.fifa_2026.dto.GebruikerRegistratieDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<ValidPasswords, GebruikerRegistratieDTO> {

    /** Vergelijkt beide wachtwoorden en koppelt een afwijking aan confirmPassword; verplichte velden worden apart gevalideerd. */
    @Override
    public boolean isValid(GebruikerRegistratieDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;
        String p1 = dto.getPassword();
        String p2 = dto.getConfirmPassword();

        if (p1 == null || p2 == null) return true; // @NotBlank handelt null af
        boolean matches = p1.equals(p2);
        if (!matches) {
            // zet fout op confirmPassword veld (handig in form)
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        return matches;
    }
}