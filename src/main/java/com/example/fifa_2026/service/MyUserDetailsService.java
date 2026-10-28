package com.example.fifa_2026.service;

import com.example.fifa_2026.model.Gebruiker;
import com.example.fifa_2026.model.Rol;
import com.example.fifa_2026.repository.GebruikerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

	private final GebruikerRepository gebruikerRepository;
	private final MessageSource messageSource;

	/** Laadt het account voor Spring Security met de opgeslagen wachtwoordhash en bijbehorende rol. */
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {

		Gebruiker user = gebruikerRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException(msg("user.username.notfound", username)));

		return new User(user.getUsername(), user.getPassword(), convertAuthorities(user.getRol()));
	}

	/** Zet de domeinrol om naar een authority met het door Spring Security verwachte ROLE_-voorvoegsel. */
	private Collection<? extends GrantedAuthority> convertAuthorities(Rol rol) {

		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_%s".formatted(rol.name())));
	}

	private String msg(String key, Object... args) {

		return messageSource.getMessage(key, args, Locale.getDefault());
	}

}