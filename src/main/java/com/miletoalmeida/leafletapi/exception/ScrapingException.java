package com.miletoalmeida.leafletapi.exception;

import lombok.Getter;

@Getter
public class ScrapingException extends RuntimeException {
    private final ScrapingErrorType errorType;
    
    public ScrapingException(String message, Throwable cause, ScrapingErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public enum ScrapingErrorType {
        RATE_LIMIT_EXCEEDED,
        PARSING_ERROR,
        NETWORK_ERROR,
        SERVICE_UNAVAILABLE,
        INVALID_RESPONSE
    }
}