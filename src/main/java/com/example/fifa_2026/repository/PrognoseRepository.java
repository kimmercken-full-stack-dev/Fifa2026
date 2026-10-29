package com.example.fifa_2026.repository;

import com.example.fifa_2026.model.Prognose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PrognoseRepository extends JpaRepository<Prognose, Long> {


    /** Projectie: alleen het gebruikers-ID en totaal laden, zonder prognose-entiteiten. */
    interface GebruikerScore {
        Long getGebruikerId();
        Long getTotaal();
    }

    /**
     * Berekent met Hibernate HQL de volledige score van gebruikers die voor deze
     * wedstrijd een prognose hebben. De WHERE-subquery selecteert die gebruikers;
     * SUM en GROUP BY tellen vervolgens ook hun prognoses voor andere wedstrijden mee.
     *
     * De CASE geeft nul bij ontbrekende scores. Een exacte voorspelling krijgt
     * exact-punten; anders wordt met SIGN het voorspelde resultaat vergeleken:
     * positief = land 1 wint, negatief = land 2 wint, nul = gelijkspel.
     *
     * COUNT telt de teams waarin deze gebruiker de enige juiste voorspeller is.
     * NOT EXISTS sluit een team uit zodra een ander lid voor dezelfde wedstrijd
     * ook exact respectievelijk het juiste resultaat voorspelde. Bij de winnaarbonus
     * telt een exacte voorspelling van een ander lid dus ook als concurrent mee.
     * LEAST begrenst basispunten plus alle teambonussen op 12 per prognose.
     *
     * De puntwaarden komen als parameters uit de i18n-bundels. De subqueries worden
     * binnen deze ene databasequery uitgevoerd, zonder repository-calls per gebruiker.
     */
    @org.springframework.data.jpa.repository.Query("""
        select p.gebruiker.id as gebruikerId,
            sum(case when p.uitslagLand1 is null or p.uitslagLand2 is null
                or w.uitslagLand1 is null or w.uitslagLand2 is null then 0
            when p.uitslagLand1 = w.uitslagLand1 and p.uitslagLand2 = w.uitslagLand2
                then least(12L, :exact + :bonusExact * (select count(t) from Team t join t.teamleden lid where lid.id = p.gebruiker.id and not exists (select q.id from Prognose q join t.teamleden ander where q.gebruiker.id = ander.id and q.gebruiker.id <> p.gebruiker.id and q.wedstrijd = w and q.uitslagLand1 = w.uitslagLand1 and q.uitslagLand2 = w.uitslagLand2)))
            when sign(p.uitslagLand1 - p.uitslagLand2) = sign(w.uitslagLand1 - w.uitslagLand2)
                then least(12L, :winnaar + :bonusWinnaar * (select count(t) from Team t join t.teamleden lid where lid.id = p.gebruiker.id and not exists (select q.id from Prognose q join t.teamleden ander where q.gebruiker.id = ander.id and q.gebruiker.id <> p.gebruiker.id and q.wedstrijd = w and sign(q.uitslagLand1 - q.uitslagLand2) = sign(w.uitslagLand1 - w.uitslagLand2))))
            else 0 end) as totaal
        from Prognose p join p.wedstrijd w
        where p.gebruiker.id in (select b.gebruiker.id from Prognose b
            where b.wedstrijd.wedstrijdID = :wedstrijdId)
        group by p.gebruiker.id
        """)
    List<GebruikerScore> berekenScoresVoorWedstrijd(
        @org.springframework.data.repository.query.Param("wedstrijdId") long wedstrijdId,
        @org.springframework.data.repository.query.Param("exact") int exact,
        @org.springframework.data.repository.query.Param("winnaar") int winnaar,
        @org.springframework.data.repository.query.Param("bonusExact") int bonusExact,
        @org.springframework.data.repository.query.Param("bonusWinnaar") int bonusWinnaar);

    List<Prognose> findAllByGebruiker_IdOrderByWedstrijd_SpeeldatumAscWedstrijd_StarttijdAsc(long gebruikerId);
    void deleteByIdAndGebruiker_Id(long prognoseId, long gebruikerId);
    List<Prognose> findAllByGebruiker_Id(long gebruikerId);
    Optional<Prognose> findByGebruiker_IdAndWedstrijd_WedstrijdID(long gebruikerId, long wedstrijdId);
    List<Prognose> findAllByWedstrijd_WedstrijdID(long wedstrijdId);
    @Transactional
    void deleteAllByWedstrijd_WedstrijdID(long wedstrijdId);
}
