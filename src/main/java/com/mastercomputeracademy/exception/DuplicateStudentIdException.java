package com.mastercomputeracademy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateStudentIdException extends RuntimeException {

    public DuplicateStudentIdException(String studentId) {
        super("Student ID already exists: " + studentId);
    }
}
