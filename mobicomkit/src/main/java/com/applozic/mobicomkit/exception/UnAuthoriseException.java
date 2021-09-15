package com.applozic.mobicomkit.exception;

//Cleanup: can be removed
public class UnAuthoriseException extends Exception {
    private String message;

    public UnAuthoriseException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}