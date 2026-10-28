package com.example.fifa_2026.config;

import com.example.fifa_2026.model.*;
import com.example.fifa_2026.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.sql.Time;
import java.time.LocalDate;

/**
 * Vult uitsluitend onder het dev-profiel fictieve gebruikers en wedstrijddata in.
 * Het gedeelde demowachtwoord komt uit lokale configuratie en wordt gehasht.
 * Combineer dit alleen met een wegwerpdatabase die bij elke start wordt hermaakt.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class InitDataConfig implements CommandLineRunner
 {
	private final TeamRepository teamRepository;
	private final GebruikerRepository gebruikerRepository;
	private final PrognoseRepository prognoseRepository;
	private final WedstrijdRepository wedstrijdRepository;
	private final LocatieRepository locatieRepository;

	 private final PasswordEncoder passwordEncoder;

    @Value("${app.demo-password}")
    private String demoPassword;

	@Override
	public void run(String... args) {

		// Gebruikers aanmaken
		if (demoPassword.isBlank() || "CHANGE_ME".equals(demoPassword)) {
            throw new IllegalArgumentException("Stel app.demo-password lokaal in voor de demoaccounts.");
        }
        String DEFAULT_PW = passwordEncoder.encode(demoPassword);

		Gebruiker jan = new Gebruiker(  Rol.ADMIN, "jan", "baard", "baardmans", 0, DEFAULT_PW);
		Gebruiker lies = new Gebruiker(  Rol.USER, "lies", "Somers", "gaming girl", 56, DEFAULT_PW);
		Gebruiker tom = new Gebruiker( Rol.USER, "tom", "everts", "winner", 114, DEFAULT_PW);
		Gebruiker steven = new Gebruiker(  Rol.USER, "steven", "deman", "steman", 38, DEFAULT_PW);
		Gebruiker ellen = new Gebruiker(  Rol.USER, "ellen", "Leers", "snellen", 79, DEFAULT_PW);
		Gebruiker alex = new Gebruiker(Rol.USER, "Alex", "Peeters", "alex", 210, DEFAULT_PW);
		Gebruiker noor = new Gebruiker(Rol.USER, "Noor", "Vermeulen", "noor", 175, DEFAULT_PW);
		Gebruiker sam = new Gebruiker(Rol.USER, "Sam", "Janssens", "sam", 132, DEFAULT_PW);
		Gebruiker mila = new Gebruiker(Rol.USER, "Mila", "Claes", "mila", 95, DEFAULT_PW);
		Gebruiker yusuf = new Gebruiker(Rol.USER, "Yusuf", "Kaya", "yusuf", 88, DEFAULT_PW);
		Gebruiker ines = new Gebruiker(Rol.USER, "Ines", "Van den Broeck", "ines", 160, DEFAULT_PW);
		Gebruiker lucas = new Gebruiker(Rol.USER, "Lucas", "Wouters", "lucas", 120, DEFAULT_PW);
		Gebruiker zoe = new Gebruiker(Rol.USER, "Zoe", "De Smet", "zoe", 140, DEFAULT_PW);
		Gebruiker nathan = new Gebruiker(Rol.USER, "Nathan", "Maes", "nathan", 65, DEFAULT_PW);
		Gebruiker emma = new Gebruiker(Rol.USER, "Emma", "Verbeeck", "emma", 155, DEFAULT_PW);

		gebruikerRepository.save(jan);
		gebruikerRepository.save(ellen);
		gebruikerRepository.save(tom);
		gebruikerRepository.save(lies);
		gebruikerRepository.save(steven);
		gebruikerRepository.save(alex);
		gebruikerRepository.save(noor);
		gebruikerRepository.save(sam);
		gebruikerRepository.save(mila);
		gebruikerRepository.save(yusuf);
		gebruikerRepository.save(ines);
		gebruikerRepository.save(lucas);
		gebruikerRepository.save(zoe);
		gebruikerRepository.save(nathan);
		gebruikerRepository.save(emma);

// Teams aanmaken
		Team team1 = new Team("team 1", null, jan);
		Team team2 = new Team("team 2", null, jan);
		Team team3 = new Team("team 3", null, steven);
		Team team4 = new Team("team 4", null, lies);
		Team team5  = new Team("team 5",  null, alex);
		Team team6  = new Team("team 6",  null, noor);
		Team team7  = new Team("team 7",  null, ines);
		Team team8  = new Team("team 8",  null, emma);
		Team team9  = new Team("team 9",  null, zoe);
		Team team10 = new Team("team 10", null, tom);
		Team team11 = new Team("team 11", null, steven);
		Team team12 = new Team("team 12", null, jan);
		Team team13 = new Team("team 13", null, lies);
		Team team14 = new Team("team 14", null, lucas);
		// Eigenaar instellen en automatisch toevoegen aan teamleden
// Team 1
		team1.setEigenaar(jan);
		team1.getTeamleden().add(jan);
		team1.getTeamleden().add(ellen);
		team1.getTeamleden().add(tom);

// Team 2
		team2.setEigenaar(jan);
		team2.getTeamleden().add(jan);

// Team 3
		team3.setEigenaar(steven);
		team3.getTeamleden().add(steven);

// Team 4
		team4.setEigenaar(lies);
		team4.getTeamleden().add(lies);

// Team 5
		team5.setEigenaar(alex);
		team5.getTeamleden().add(alex);
		team5.getTeamleden().add(ines);
		team5.getTeamleden().add(noor);
		team5.getTeamleden().add(steven);

// Team 6
		team6.setEigenaar(noor);
		team6.getTeamleden().add(noor);
		team6.getTeamleden().add(zoe);
		team6.getTeamleden().add(sam);

// Team 7
		team7.setEigenaar(ines);
		team7.getTeamleden().add(ines);
		team7.getTeamleden().add(emma);
		team7.getTeamleden().add(lucas);

// Team 8
		team8.setEigenaar(emma);
		team8.getTeamleden().add(emma);
		team8.getTeamleden().add(alex);
		team8.getTeamleden().add(tom);

// Team 9
		team9.setEigenaar(zoe);
		team9.getTeamleden().add(zoe);
		team9.getTeamleden().add(noor);
		team9.getTeamleden().add(mila);

// Team 10
		team10.setEigenaar(tom);
		team10.getTeamleden().add(tom);
		team10.getTeamleden().add(yusuf);
		team10.getTeamleden().add(nathan);

// Team 11
		team11.setEigenaar(steven);
		team11.getTeamleden().add(steven);
		team11.getTeamleden().add(ellen);
		team11.getTeamleden().add(sam);

// Team 12
		team12.setEigenaar(jan);
		team12.getTeamleden().add(jan);
		team12.getTeamleden().add(lies);
		team12.getTeamleden().add(emma);

// Team 13
		team13.setEigenaar(lies);
		team13.getTeamleden().add(lies);
		team13.getTeamleden().add(mila);
		team13.getTeamleden().add(nathan);

// Team 14
		team14.setEigenaar(lucas);
		team14.getTeamleden().add(lucas);
		team14.getTeamleden().add(ines);
		team14.getTeamleden().add(zoe);


		teamRepository.save(team1);
		teamRepository.save(team2);
		teamRepository.save(team3);
		teamRepository.save(team4);
		teamRepository.save(team5);
		teamRepository.save(team6);
		teamRepository.save(team7);
		teamRepository.save(team8);
		teamRepository.save(team9);
		teamRepository.save(team10);
		teamRepository.save(team11);
		teamRepository.save(team12);
		teamRepository.save(team13);
		teamRepository.save(team14);

// Locaties aanmaken
		Locatie loc1 = locatieRepository.save(new Locatie("Brussel", "Koning Boudewijnstadion", 1234, 10000));
		Locatie loc2 = locatieRepository.save(new Locatie("Parijs", "Stade de France", 2345, 15000));
		Locatie loc3 = locatieRepository.save(new Locatie("Berlijn", "Olympiastadion", 3456, 17000));
		Locatie loc4 = locatieRepository.save(new Locatie("Madrid", "Santiago Bernabeu", 4567, 20000));
		Locatie loc5 = locatieRepository.save(new Locatie("Toronto", "BMO Field", 5678, 45000));
		Locatie loc6 = locatieRepository.save(new Locatie("Vancouver", "BC Place", 6789, 54000));
		Locatie loc7 = locatieRepository.save(new Locatie("Mexico City", "Estadio Azteca", 7890, 87000));
		Locatie loc8 = locatieRepository.save(new Locatie("Guadalajara", "Estadio Akron", 8901, 48000));
		Locatie loc9 = locatieRepository.save(new Locatie("Los Angeles", "SoFi Stadium", 9012, 70000));
		Locatie loc10 = locatieRepository.save(new Locatie("New York", "MetLife Stadium", 1122, 82500));
		Locatie loc11 = locatieRepository.save(new Locatie("Dallas", "AT&T Stadium", 2233, 80000));
		Locatie loc12 = locatieRepository.save(new Locatie("Miami", "Hard Rock Stadium", 3344, 65000));

// Wedstrijden aanmaken
		Wedstrijd w1 = new Wedstrijd("Belgie", "Nederland", 2, 1, "Belgie",
				LocalDate.of(2026, 4, 18), Time.valueOf("16:01:00"), loc1);

		Wedstrijd w2 = new Wedstrijd("Frankrijk", "Nederland", 0, 1, "Nederland",
				LocalDate.of(2026, 4, 18), Time.valueOf("16:00:00"), loc2);

		Wedstrijd w3 = new Wedstrijd("Duitsland", "Spanje", null, null, null,
				LocalDate.of(2026, 10, 23), Time.valueOf("14:00:00"), loc3);

		Wedstrijd w4 = new Wedstrijd("Spanje", "Nederland", null, null, null,
				LocalDate.of(2026, 11, 10), Time.valueOf("18:30:00"), loc4);
		Wedstrijd w5 = new Wedstrijd("Italie", "Portugal", null, null, null,
				LocalDate.of(2026, 5, 20), Time.valueOf("20:00:00"), loc5);

		Wedstrijd w6 = new Wedstrijd("Engeland", "Kroatie", null, null, null,
				LocalDate.of(2026, 6, 2), Time.valueOf("18:00:00"), loc6);

		Wedstrijd w7 = new Wedstrijd("Argentinie", "Brazilie", null, null, null,
				LocalDate.of(2026, 6, 15), Time.valueOf("21:00:00"), loc7);

		Wedstrijd w8 = new Wedstrijd("USA", "Mexico", null, null, null,
				LocalDate.of(2026, 7, 1), Time.valueOf("19:30:00"), loc8);

		Wedstrijd w9 = new Wedstrijd("Japan", "Zuid-Korea", null, null, null,
				LocalDate.of(2026, 8, 9), Time.valueOf("14:00:00"), loc9);

		Wedstrijd w10 = new Wedstrijd("Marokko", "Senegal", null, null, null,
				LocalDate.of(2026, 9, 5), Time.valueOf("16:00:00"), loc10);

		Wedstrijd w11 = new Wedstrijd("Zwitserland", "Oostenrijk", null, null, null,
				LocalDate.of(2026, 10, 5), Time.valueOf("17:15:00"), loc11);

		Wedstrijd w12 = new Wedstrijd("Polen", "Tsjechie", null, null, null,
				LocalDate.of(2026, 11, 25), Time.valueOf("20:45:00"), loc12);

		Wedstrijd w13 = new Wedstrijd("Belgie", "Duitsland", null, null, null,
				LocalDate.of(2026, 12, 5), Time.valueOf("20:00:00"), loc1);

		Wedstrijd w14 = new Wedstrijd("Nederland", "Frankrijk", null, null, null,
				LocalDate.of(2026, 12, 2), Time.valueOf("15:00:00"), loc2);
		wedstrijdRepository.save(w1);
		wedstrijdRepository.save(w2);
		wedstrijdRepository.save(w3);
		wedstrijdRepository.save(w4);
		wedstrijdRepository.save(w5);
		wedstrijdRepository.save(w6);
		wedstrijdRepository.save(w7);
		wedstrijdRepository.save(w8);
		wedstrijdRepository.save(w9);
		wedstrijdRepository.save(w10);
		wedstrijdRepository.save(w11);
		wedstrijdRepository.save(w12);
		wedstrijdRepository.save(w13);
		wedstrijdRepository.save(w14);


// Prognoses aanmaken
		Prognose p1 = new Prognose(1, 2, 3);
		p1.setGebruiker(jan);
		p1.setWedstrijd(w1);
		prognoseRepository.save(p1);
		Prognose p8 = new Prognose(1, 2, 3);
		p8.setGebruiker(jan);
		p8.setWedstrijd(w5);
		prognoseRepository.save(p8);
		Prognose p9 = new Prognose(1, 1, 0);
		p9.setGebruiker(ellen);
		p9.setWedstrijd(w5);
		prognoseRepository.save(p9);
		Prognose p2 = new Prognose(2, 0, 2);
		p2.setGebruiker(ellen);
		p2.setWedstrijd(w3);
		prognoseRepository.save(p2);
		Prognose p3 = new Prognose(3, 3, 2);
		p3.setGebruiker(lies);
		p3.setWedstrijd(w1);
		prognoseRepository.save(p3);
		Prognose p4 = new Prognose(4, 0, 1);
		p4.setGebruiker(steven);
		p4.setWedstrijd(w2);
		prognoseRepository.save(p4);
		Prognose p5 = new Prognose(5, 2, 3);
		p5.setGebruiker(tom);
		p5.setWedstrijd(w2);
		prognoseRepository.save(p5);
		Prognose p6 = new Prognose(6, 1, 3);
		p6.setGebruiker(jan);
		p6.setWedstrijd(w4);
		prognoseRepository.save(p6);
		Prognose p7 = new Prognose(7, 2, 2);
		p7.setGebruiker(steven);
		p7.setWedstrijd(w1);
		prognoseRepository.save(p7);

		jan.addTeam(team1);
		jan.addTeam(team3);
		ellen.addTeam(team1);
		ellen.addTeam(team2);
		lies.addTeam(team4);
		steven.addTeam(team3);
		alex.addTeam(team5);
		ines.addTeam(team5);
		noor.addTeam(team5);

		noor.addTeam(team6);
		zoe.addTeam(team6);
		sam.addTeam(team6);

		ines.addTeam(team7);
		emma.addTeam(team7);
		lucas.addTeam(team7);

		emma.addTeam(team8);
		alex.addTeam(team8);
		tom.addTeam(team8);

		zoe.addTeam(team9);
		noor.addTeam(team9);
		mila.addTeam(team9);

		tom.addTeam(team10);
		yusuf.addTeam(team10);

		wedstrijdRepository.save(w1);
		wedstrijdRepository.save(w2);
		wedstrijdRepository.save(w3);
		wedstrijdRepository.save(w4);


	}

}
