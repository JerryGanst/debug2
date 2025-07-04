package org.example.ai_api.Exception;

public class SyncApiException extends ApiException {
    public SyncApiException(int code, String message) {
        super(code, message);
    }
}
