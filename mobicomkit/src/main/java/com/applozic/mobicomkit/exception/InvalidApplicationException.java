package com.applozic.mobicomkit.exception;

//Cleanup: can be removed
public class InvalidApplicationException extends Exception {
    private String message;

    public InvalidApplicationException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}