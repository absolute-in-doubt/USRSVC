package com.innowise.userservice.infrastructure.cache;

import com.innowise.userservice.domain.model.exception.FailedToPerformOperationException;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface TwoLevelCacheService {

    <T> T get(String cacheName, String key, Type returnType, Callable<T> dbLoader)
            throws Exception;

    void put(String cacheName, String key, Object value);

    void evict(String cacheName, String key);

    void evictAll(String cacheName);
}
