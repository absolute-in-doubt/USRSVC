package com.innowise.userservice.infrastructure.cache;

import com.innowise.userservice.domain.model.exception.FailedToPerformOperationException;

import java.lang.reflect.Type;
import java.util.function.Supplier;

public interface TwoLevelCacheService {

    public <T> T get(String cacheName, String key, Type returnType, Supplier<T> dbLoader)
            throws FailedToPerformOperationException;

    void put(String cacheName, String key, Object value);

    void evict(String cacheName, String key);

    void evictAll(String cacheName);

    boolean isCachingOn();

    void setCachingOn(boolean cachingOn);
}
