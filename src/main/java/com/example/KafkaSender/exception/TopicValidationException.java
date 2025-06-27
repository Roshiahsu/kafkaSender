package com.example.KafkaSender.exception;

public class TopicValidationException extends RuntimeException{
    public TopicValidationException(String message) {
        super(message);
    }
    public TopicValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
