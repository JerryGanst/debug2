package org.example.ai_api.Exception;

public class RetryableApiException extends RuntimeException {
    public RetryableApiException(String message) {
        super(message);
    }

    public RetryableApiException(int statusCode, String error) {
    }
}
