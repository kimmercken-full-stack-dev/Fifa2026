package com.example.fifa_2026.validator;

import com.example.fifa_2026.dto.WedstrijdCreateDTO;
import com.example.fifa_2026.dto.WedstrijdEditDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

/**
 * Valideert de vaste opdrachtperiode in 2026 en de minimale voorbereidingstijd.
 * De controle gebruikt de systeemklok; dit is geen jaarlijks doorschuivend toernooi.
 */
@Component
public class WedstrijdMomentValidator
        implements ConstraintValidator<ValidWedstrijdMoment, Object> {

    private static final LocalDate START_DATUM =
            LocalDate.of(2026, 4, 5);

    private static final LocalDate EIND_DATUM =
            LocalDate.of(2026, 12, 5);

    @Autowired(required = false)
    private MessageSource messageSource;

    /** Controleert de opdrachtperiode, een toekomstig wedstrijdmoment en minimaal twee uur voorbereiding bij wedstrijden vandaag. */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        String speeldatumString;
        String starttijdString;

        if (value instanceof WedstrijdCreateDTO dto) {
            speeldatumString = dto.getSpeeldatum();
            starttijdString = dto.getStarttijd();
        } else if (value instanceof WedstrijdEditDTO dto) {
            speeldatumString = dto.getSpeeldatum();
            starttijdString = dto.getStarttijd();
        } else {
            return true;
        }

        if (speeldatumString == null
                || starttijdString == null) {
            return true;
        }

        LocalDate speeldatum;
        LocalTime starttijd;

        try {
            speeldatum = LocalDate.parse(speeldatumString);
            starttijd = LocalTime.parse(starttijdString);
        } catch (Exception e) {
            return true;
        }

        Locale locale = LocaleContextHolder.getLocale();

        // datum range
        if (speeldatum.isBefore(START_DATUM)
                || speeldatum.isAfter(EIND_DATUM)) {

            String msg = message("wedstrijd.speeldatum.range",
                    new Object[]{START_DATUM, EIND_DATUM}, locale,
                    "Wedstrijddatum valt buiten de toegestane periode");

            addFieldError(context, "speeldatum", msg);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime wedstrijdMoment = LocalDateTime.of(speeldatum, starttijd);

        // verleden
        if (wedstrijdMoment.isBefore(now)) {
            String msg = message("wedstrijd.speeldatum.past", null, locale,
                    "Wedstrijddatum mag niet in het verleden liggen");
            addFieldError(context, "speeldatum", msg);
            return false;
        }

        // vandaag + 2 uur
        if (speeldatum.equals(now.toLocalDate())) {
            if (wedstrijdMoment.isBefore(now.plusHours(2))) {
                String msg = message("wedstrijd.starttijd.today", null, locale,
                        "Wedstrijd moet minstens twee uur in de toekomst liggen");
                addFieldError(context, "starttijd", msg);
                return false;
            }
        }
        return true;
    }

    private void addFieldError(ConstraintValidatorContext context, String field, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }

    private String message(String key, Object[] args, Locale locale, String fallback) {
        if (messageSource == null) {
            return fallback;
        }
        return messageSource.getMessage(key, args, fallback, locale);
    }
}
