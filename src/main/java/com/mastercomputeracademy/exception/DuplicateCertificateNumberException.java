package com.mastercomputeracademy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCertificateNumberException extends RuntimeException {

    public DuplicateCertificateNumberException(String certificateNumber) {
        super("Certificate number already exists: " + certificateNumber);
    }
}
