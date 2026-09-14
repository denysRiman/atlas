package com.example.atlas.exception;

public class ProviderAuthenticationException extends RuntimeException {
    public ProviderAuthenticationException(String message, Exception e) {
        super(message, e);
    }
}
