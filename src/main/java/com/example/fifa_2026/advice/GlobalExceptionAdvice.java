package com.example.fifa_2026.advice;

//import com.example.spring_boot_exceptions.exception.ReservationNotAvailableException;
import com.example.fifa_2026.exception.wedstrijdmomentNietBeschikbaarInDitStadionException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(wedstrijdmomentNietBeschikbaarInDitStadionException.class)
    public String handleWedstrijdmomentNietBeschikbaarInDitStadion(
            wedstrijdmomentNietBeschikbaarInDitStadionException ex,
            Model model) {

        model.addAttribute("exception", ex);
        return "error/wedstrijdmomentNietBeschikbaarInDitStadion";
    }

    @ExceptionHandler(NumberFormatException.class)
    public String handleNumberFormat(
            NumberFormatException ex) {

        return "error/generic_error";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleGeneric(RuntimeException ex, Model model) {

        model.addAttribute("message", "An error has occurred. Please contact our administrator for details.");
        return "error/error";
    }
}
