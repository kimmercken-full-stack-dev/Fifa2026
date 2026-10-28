package com.example.fifa_2026.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Combineert URL-beveiliging met methodebeveiliging via PreAuthorize.
 * Formulierlogin gebruikt databasegebruikers en BCrypt; CSRF blijft ingeschakeld.
 */
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/style.css",
                                "/img/**",
                                "/locaties/**",
                                "/wedstrijden/{speeldatum}",
                                "/css/**", "/js/**", "/images/**",
                                "/login**", "/registreer", "/registreer/**", "/404**",
                                "/changeLocale"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/", "/").permitAll()

                        .requestMatchers(HttpMethod.GET, "/wedstrijd", "/wedstrijd/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/teams", "/teams/").permitAll()
                        //.requestMatchers(HttpMethod.GET, "/wedstrijd/{wedstrijdId}").authenticated()
                        //.requestMatchers(HttpMethod.GET, "/wedstrijd/*").authenticated()

                        .requestMatchers("/wedstrijd/nieuw", "/wedstrijd/nieuw/**", "/wedstrijd/*/edit-uitslag", "/wedstrijd/*/edit-uitslag/**",
                                 "/wedstrijd/*/edit", "/wedstrijd/*/edit/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/wedstrijd", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/wedstrijd?logout")
                        .permitAll()
                )
                .exceptionHandling(handling -> handling.accessDeniedPage("/403"))
                .csrf(Customizer.withDefaults());

        return http.build();
    }
}
