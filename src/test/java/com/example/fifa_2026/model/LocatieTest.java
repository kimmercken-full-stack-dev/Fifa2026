package com.example.fifa_2026.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocatieTest {

    @Test
    void prePersist_computesChecksumFromStadiumcode() throws Exception {
        Locatie locatie = new Locatie("Brussel", "Koning Boudewijnstadion", 1234, 50000);

        invokePrePersist(locatie);

        assertEquals(1234 % 97, locatie.getChecksum());
    }

    @Test
    void prePersist_rejectsInvalidStadiumcode() {
        Locatie locatie = new Locatie("Brussel", "Koning Boudewijnstadion", 123, 50000);

        InvocationTargetException exception =
                assertThrows(InvocationTargetException.class, () -> invokePrePersist(locatie));

        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
    }

    @Test
    void prePersist_rejectsIncorrectChecksum() {
        Locatie locatie = new Locatie("Brussel", "Koning Boudewijnstadion", 1234, 50000);
        locatie.setChecksum(1);

        InvocationTargetException exception =
                assertThrows(InvocationTargetException.class, () -> invokePrePersist(locatie));

        assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
    }

    @Test
    void prePersist_acceptsCorrectChecksum() throws Exception {
        Locatie locatie = new Locatie("Brussel", "Koning Boudewijnstadion", 1234, 50000);
        locatie.setChecksum(1234 % 97);

        invokePrePersist(locatie);

        assertEquals(1234 % 97, locatie.getChecksum());
    }

    private void invokePrePersist(Locatie locatie) throws Exception {
        Method method = Locatie.class.getDeclaredMethod("validateAndComputeChecksum");
        method.setAccessible(true);
        method.invoke(locatie);
    }
}
