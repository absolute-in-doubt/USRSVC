package com.innowise.userservice.infrastructure.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.caching")
public record CacheProperties(boolean logging, Local local, Remote remote) {

    public record Local(int maxSize, int expirationMin) {}

    public record Remote(int staleAfterMin, int ttlMin) {}
}
