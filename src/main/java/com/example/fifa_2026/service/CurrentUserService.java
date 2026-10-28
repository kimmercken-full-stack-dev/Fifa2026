package com.example.fifa_2026.service;

import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.repository.GebruikerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Verbindt de ingelogde Spring Security-principal met de gebruiker in de database. */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final GebruikerRepository gebruikerRepository;
    private final MessageSource messageSource;

    /** Leest de gebruikersnaam uit de beveiligingscontext en weigert anonieme verzoeken. */
    public String getCurrentUsernameOrThrow() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException(
                    messageSource.getMessage("error.niet.ingelogd", null, Locale.getDefault()));
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) { return ud.getUsername();}
        if (principal instanceof String s) {return s;}

        throw new IllegalStateException(
                messageSource.getMessage("error.principal.onbekend", new Object[]{principal.getClass()}, Locale.getDefault()));
    }

    /** Zoekt het databaseaccount op dat bij de ingelogde gebruiker hoort. */
    @Transactional(readOnly = true)
    public Gebruiker getCurrentGebruikerOrThrow() {

        String username = getCurrentUsernameOrThrow();

        return gebruikerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage("error.gebruiker.nietgevonden.username", new Object[]{username}, Locale.getDefault())));
    }

    @Transactional(readOnly = true)
    public long getCurrentGebruikerIdOrThrow() {
        return getCurrentGebruikerOrThrow().getId();
    }

    /** Controleert of de huidige authenticatie de authority ROLE_ADMIN bevat. */
    @Transactional(readOnly = true)
    public boolean isAdmin() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return false;
        }

        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
