package com.example.atlas.exception;

public class ProviderTimeoutException extends RuntimeException {
    public ProviderTimeoutException(String message) {
        super(message);
    }
}
