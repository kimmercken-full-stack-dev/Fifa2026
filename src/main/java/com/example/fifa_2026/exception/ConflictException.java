package com.example.fifa_2026.exception;

public class ConflictException extends IllegalStateException {
    public ConflictException(String bericht) {
        super(bericht);
    }
}
