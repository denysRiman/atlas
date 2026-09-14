package com.example.atlas.exception;

public class ProviderMisconfigurationException extends RuntimeException {
    public ProviderMisconfigurationException(String message, Exception e) {
        super(message, e);
    }
}
