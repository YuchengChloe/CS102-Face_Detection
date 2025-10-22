package com.smartattendance.repository.exceptions;

public class SessionNotFoundException extends RuntimeException{
    public SessionNotFoundException(String message) {
        super(message);
    }
}
