package com.example.atlas.exception;

public class ProviderUnavailableException extends RuntimeException {
    public ProviderUnavailableException(String message, Exception e) {
        super(message, e);
    }
}
