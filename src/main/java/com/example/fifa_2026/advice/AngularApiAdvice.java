package com.example.fifa_2026.advice;

import com.example.fifa_2026.restControllers.WedstrijdRestController;
import com.example.fifa_2026.exception.wedstrijdmomentNietBeschikbaarInDitStadionException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Order(0)
@RestControllerAdvice(basePackageClasses = WedstrijdRestController.class)
public class AngularApiAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validatie(MethodArgumentNotValidException ex) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Controleer de ingevulde gegevens.");
        detail.setProperty("fouten", ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage()).toList());
        return detail;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail status(ResponseStatusException ex) {
        return ex.getBody();
    }

    @ExceptionHandler(wedstrijdmomentNietBeschikbaarInDitStadionException.class)
    public ProblemDetail overlap() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Het stadion is op dat tijdstip bezet.");
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ProblemDetail bedrijfsregel(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
