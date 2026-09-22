package com.example.atlas.exception;

public class ConversationVersionConflictException extends RuntimeException {
    public ConversationVersionConflictException(String message) {
        super(message);
    }
}
