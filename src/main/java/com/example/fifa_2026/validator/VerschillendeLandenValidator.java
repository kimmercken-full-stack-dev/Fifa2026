package com.example.fifa_2026.validator;

import com.example.fifa_2026.dto.WedstrijdCreateDTO;
import com.example.fifa_2026.dto.WedstrijdEditDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class VerschillendeLandenValidator
        implements ConstraintValidator<VerschillendeLanden, Object> {

    @Autowired(required = false)
    private MessageSource messageSource;

    /** Vergelijkt de landen hoofdletterongevoelig en koppelt gelijke landen aan een fout op land2. */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        String land1;
        String land2;

        if (value instanceof WedstrijdCreateDTO dto) {

            land1 = dto.getLand1();
            land2 = dto.getLand2();

        } else if (value instanceof WedstrijdEditDTO dto) {

            land1 = dto.getLand1();
            land2 = dto.getLand2();

        } else {return true;}

        if (land1 == null || land2 == null) {return true;}

        if (land1.equalsIgnoreCase(land2)) {
            Locale locale = LocaleContextHolder.getLocale();
            String msg = messageSource == null
                    ? "Land 1 en Land 2 moeten verschillend zijn"
                    : messageSource.getMessage("wedstrijd.landen.verschillend", null,
                            "Land 1 en Land 2 moeten verschillend zijn", locale);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(msg)
                    .addPropertyNode("land2")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
