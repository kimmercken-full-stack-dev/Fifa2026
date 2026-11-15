package com.example.fifa_2026.advice;

import com.example.fifa_2026.exception.ConflictException;
import com.example.fifa_2026.exception.GeenToegangException;
import com.example.fifa_2026.exception.NietGevondenException;
import com.example.fifa_2026.exception.wedstrijdmomentNietBeschikbaarInDitStadionException;
import com.example.fifa_2026.restControllers.WedstrijdRestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Order(0)
@RestControllerAdvice(basePackageClasses = WedstrijdRestController.class)
public class AngularApiAdvice extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Controleer de ingevulde gegevens.");
        detail.setProperty("fouten", ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage()).toList());
        return handleExceptionInternal(ex, detail, headers, status, request);
    }

    @ExceptionHandler(NietGevondenException.class)
    public ProblemDetail nietGevonden(NietGevondenException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(GeenToegangException.class)
    public ProblemDetail geenToegang(GeenToegangException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail toegangGeweigerd() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Je hebt geen toegang tot deze actie.");
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail conflict(ConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(wedstrijdmomentNietBeschikbaarInDitStadionException.class)
    public ProblemDetail overlap() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Het stadion is op dat tijdstip bezet.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail onverwachteFout(RuntimeException ex) {
        log.error("Onverwachte fout bij een REST-verzoek.", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Er is een onverwachte fout opgetreden.");
    }
}
