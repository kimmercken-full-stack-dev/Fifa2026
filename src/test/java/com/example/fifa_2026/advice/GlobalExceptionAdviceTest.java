package com.example.fifa_2026.advice;

import com.example.fifa_2026.exception.wedstrijdmomentNietBeschikbaarInDitStadionException;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GlobalExceptionAdviceTest {

    private final GlobalExceptionAdvice advice = new GlobalExceptionAdvice();

    @Test
    void wedstrijdmomentException_returnsSpecificErrorPage() {
        Model model = mock(Model.class);
        var exception = new wedstrijdmomentNietBeschikbaarInDitStadionException();

        String view = advice.handleWedstrijdmomentNietBeschikbaarInDitStadion(exception, model);

        assertEquals("error/wedstrijdmomentNietBeschikbaarInDitStadion", view);
        verify(model).addAttribute("exception", exception);
    }

    @Test
    void numberFormatException_returnsGenericErrorPage() {
        String view = advice.handleNumberFormat(new NumberFormatException("bad id"));

        assertEquals("error/generic_error", view);
    }

    @Test
    void runtimeException_returnsErrorPageWithMessage() {
        Model model = mock(Model.class);

        String view = advice.handleGeneric(new RuntimeException("boom"), model);

        assertEquals("error/error", view);
        verify(model).addAttribute(
                "message",
                "An error has occurred. Please contact our administrator for details.");
    }
}
