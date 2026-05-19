package com.innowise.userservice.infrastructure.cache;

import java.time.LocalDateTime;

public record CacheEnvelope<T>(
        T data,
        LocalDateTime freshUntil
) {
    boolean isFresh(){
        return LocalDateTime.now().isBefore(freshUntil);
    }
}
