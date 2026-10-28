package com.example.fifa_2026;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class Fifa2026Application implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(Fifa2026Application.class, args);
    }
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/wedstrijd");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/403").setViewName("error/403");
    }

}
