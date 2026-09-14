package com.example.atlas.exception;

public class InternalException extends RuntimeException {
    public InternalException(String message, Throwable e) {
        super(message, e);
    }
}
