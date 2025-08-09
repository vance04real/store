package com.example.store.payload.response;

import java.time.Instant;

public record ErrorResponse(String message,
                            Instant timestamp,
                            String path,
                            int status) {
    public static ErrorResponse of(String message, String path, int status) {
        return new ErrorResponse(message, Instant.now(), path, status);
    }
}
