package com.auditor.gemini;

public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) { super(message); } 
}
