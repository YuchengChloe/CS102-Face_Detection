package com.smartattendance.repository.exceptions;

public class DuplicateStudentIDException extends RuntimeException{
    public DuplicateStudentIDException(String message) {
        super(message);
    }
}
