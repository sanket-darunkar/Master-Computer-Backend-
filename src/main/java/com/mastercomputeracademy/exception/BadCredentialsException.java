package com.mastercomputeracademy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class BadCredentialsException extends RuntimeException {

    public BadCredentialsException() {
        super("Invalid email or password");
    }

    public BadCredentialsException(String message) {
        super(message);
    }
}
