package com.example.fifa_2026.init;

import java.sql.Time;
import java.time.LocalDate;

public class InitWedstrijd {
    public static final String OK_LAND1 ="Spanje";
    public static final String OK_LAND2 = "Nederland";
    public static final String OK_SPEELDATUM = LocalDate.of(2026, 11, 2).toString();
    public static final String OK_STARTTIJD = Time.valueOf("15:00:00").toString();
    public static final Long OK_LOCATIEID = 3L;
}
