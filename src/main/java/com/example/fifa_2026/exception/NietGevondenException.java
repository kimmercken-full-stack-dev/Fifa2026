package com.example.fifa_2026.exception;

public class NietGevondenException extends IllegalArgumentException {
    public NietGevondenException(String bericht) {
        super(bericht);
    }
}
