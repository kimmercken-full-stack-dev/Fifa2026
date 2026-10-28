package com.example.fifa_2026.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class LocaleController {

    private final LocaleResolver localeResolver;

    @GetMapping("/changeLocale")
    public String changeLocale(HttpServletRequest request, HttpServletResponse response, @RequestParam("lang") String lang) {
        Locale locale = Locale.forLanguageTag(lang);
        localeResolver.setLocale(request, response, locale);
        return "redirect:" + request.getHeader("Referer");
    }
}