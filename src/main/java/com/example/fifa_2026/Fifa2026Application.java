package com.example.fifa_2026;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;
import java.time.ZoneId;

@SpringBootApplication
public class Fifa2026Application implements WebMvcConfigurer {

    @Bean
    Clock wedstrijdKlok(@Value("${app.wedstrijd-zone:Europe/Brussels}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }

    @Bean
    ValidationConfigurationCustomizer validatieKlok(Clock clock) {
        return configuration -> configuration.clockProvider(() -> clock);
    }

    public static void main(String[] args) {
        SpringApplication.run(Fifa2026Application.class, args);
    }
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/wedstrijd");
        registry.addRedirectViewController("/wedstrijd/", "/wedstrijd");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/403").setViewName("error/403");
    }

}
