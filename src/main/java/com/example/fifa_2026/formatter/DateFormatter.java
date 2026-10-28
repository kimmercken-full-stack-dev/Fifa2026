package com.example.fifa_2026.formatter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.expression.ParseException;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RequiredArgsConstructor
@Component
public class DateFormatter implements Formatter<LocalDate> {

        private final MessageSource messageSource;

        @Override
        public String print(LocalDate object, Locale locale) {
            return object.format(formatter(locale));
        }

        @Override
        public LocalDate parse(String text, Locale locale) throws ParseException {
            return LocalDate.parse(text, formatter(locale));
        }

        private DateTimeFormatter formatter(Locale locale) {
            return DateTimeFormatter.ofPattern(
                    messageSource.getMessage("date.format.pattern", null, locale),
                    locale);
        }
}