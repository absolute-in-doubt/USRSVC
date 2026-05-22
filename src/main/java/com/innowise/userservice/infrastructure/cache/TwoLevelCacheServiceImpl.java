package com.innowise.userservice.infrastructure.cache;

import com.innowise.userservice.domain.model.exception.FailedToPerformOperationException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Call;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.web.config.SpringDataJackson3Configuration;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * L1 cache - Caffeine contains CacheEnvelopes which store raw objects as data (Here freshUntil isn't used)
 * L2 cache - Redis contains json serialized CacheEnvelopes, which store raw objects as data
 */

@Slf4j
@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class TwoLevelCacheServiceImpl implements TwoLevelCacheService{
    private final CacheManager caffeineManager;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper mapper;
    private final CacheProperties cacheProperties;

    private final boolean cacheLoggingOn;

    public TwoLevelCacheServiceImpl(
            CacheManager caffeineManager,
            StringRedisTemplate redisTemplate,
            @Lazy ObjectMapper mapper,
            CacheProperties cacheProperties
    ) {
        this.caffeineManager = caffeineManager;
        this.redisTemplate = redisTemplate;
        this.cacheProperties = cacheProperties;
        this.cacheLoggingOn = cacheProperties.logging();

        this.mapper = mapper.rebuild()
                .addModule(
                        new SpringDataJackson3Configuration().jackson3pageModule()
                )
                .build();

    }

    @Override
    public <T> T get(String cacheName, String key, Type returnType, Callable<T> dbLoader) throws Exception {


        CacheEnvelope<T> l1Result = getL1(cacheName, key);

        if(l1Result != null) {
            if(cacheLoggingOn)
                log.trace("L1 {} hit for {} with key <{}>", cacheName,l1Result.data(), key);

            return l1Result.data(); //may as well return null
        }

        CacheEnvelope<T> l2Result = getL2(cacheName, key, returnType);
        if(l2Result != null){
            if(l2Result.isFresh()) {
                    T result = l2Result.data();
                    putL1(cacheName, key, l2Result);
                    if(cacheLoggingOn)
                        log.trace("L2 {} hit for {} with key <{}>",cacheName, result, key);

                    return result;
            } else { //value exists, but it's stale
                if(cacheLoggingOn)
                    log.trace("L2 {} hit (stale) for {} with key <{}>\nInitiating async update", cacheName, l2Result.data(), key);
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return loadWithLockAndSecondCheck(cacheName, key, returnType, dbLoader);
                    } catch (Exception e) {
                        log.error("Error loading the value from DB for cache {}:<{}>\n", cacheName, key, e);
                        return null;
                    }
                });
                return l2Result.data();
            }
        }
        // all the caches are missed
        // same thing but synchronously
        T result = loadWithLockAndSecondCheck(cacheName, key,  returnType, dbLoader);
        if(cacheLoggingOn)
            log.trace("{} miss for {} with key <{}>", cacheName, result, key);
        return result;
    }

    @Override
    public void put(String cacheName, String key, Object value) {
            CacheEnvelope<Object> envelope = new CacheEnvelope<>(value, LocalDateTime.now()
                    .plus(Duration.ofMinutes(cacheProperties.remote().staleAfterMin())));
            putL1(cacheName, key, envelope);
            putL2(cacheName, key, envelope);
            log.trace("{} put for {} with key <{}>", cacheName,value, key);
    }

    @Override
    public void evict(String cacheName, String key) {
        evictL1(cacheName, key);
        evictL2(cacheName, key);
        log.trace("Cache evict for key <{}> for {}", key, cacheName);
    }

    @Override
    public void evictAll(String cacheName) {
        evictAllL1(cacheName);
        evictAllL2(cacheName);
        log.trace("Evict All for {}", cacheName);
    }

    private <T> CacheEnvelope<T> getL1(String cacheName, String key) {
        // We don't convert the value before putting it into the Caffeine, so this should be fine
        // It's just casted here from Object back to CacheEnvelope<T>
        return (CacheEnvelope<T>) Objects.requireNonNull(caffeineManager.getCache(cacheName)).get(key, CacheEnvelope.class);
    }

    private <T> CacheEnvelope<T> getL2(String cacheName, String key, Type returnType){
        String json = redisTemplate.opsForValue().get(cacheName + ":" + key);
        if(json == null)
            return null;

        JavaType javaType =
                mapper.getTypeFactory()
                        .constructType(returnType);

        if(javaType.isTypeOrSubTypeOf(Page.class)) {

            JavaType contentType =
                    javaType.getBindings().getBoundType(0);

            javaType =
                    mapper.getTypeFactory()
                            .constructParametricType(
                                    PageImpl.class,
                                    contentType
                            );
        }
        var root = mapper.readTree(json);
        String dataJson = root.get("data").toString();
        T data = mapper.readValue(dataJson, javaType);
        LocalDateTime freshUntil = mapper.treeToValue(root.get("freshUntil"), LocalDateTime.class);
        return new CacheEnvelope<T>(data, freshUntil);
    }

    private <T> T loadWithLockAndSecondCheck(String cacheName,
                                             String key,
                                             Type returnType,
                                             Callable<T> dbLoader) throws Exception {
        String lockKey = "lock:" + cacheName + ":" +  key;
        boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(60));

        long timeoutMs = 100;
        while(!acquired){
            try {
                if(timeoutMs > 10000) {
                    log.warn("Couldn't acquire cache lock for {}:<{}>", cacheName, key);
                    throw new FailedToPerformOperationException("Failed to retrieve cache lock to load the value: " + cacheName + ":" + key);
                }
                Thread.sleep(timeoutMs);
                timeoutMs *= 2;
                acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(60));
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for the cache lock for {}:<{}>", cacheName, key);
                throw new FailedToPerformOperationException("Interrupted while waiting for the cache lock for  " + cacheName + ":" + key);
            }
        }

        //Second check after the lock is retrieved,
        //whether the value was already loaded by another thread
        try {
            CacheEnvelope<T> l1Result = getL1(cacheName, key);
            if (l1Result != null)
                return l1Result.data();

            CacheEnvelope<T> envelope = getL2(cacheName, key, returnType);
            if (envelope != null && envelope.isFresh()) {
                putL1(cacheName, key, envelope);
                return envelope.data();
            }

            T value = dbLoader.call();  //NOTE: even if the DB operation returns null -> wrap it and store as it is to avid czche penetration
            CacheEnvelope<T> cacheEnvelope = new CacheEnvelope<>(value, LocalDateTime.now().plus(Duration.ofMinutes(cacheProperties.remote().staleAfterMin())));
            put(cacheName, key, value);
            return value;
        } finally{
            redisTemplate.delete(lockKey);
        }

    }


    private <T> void putL1(String cacheName, String key, CacheEnvelope<T> envelope) {
        Objects.requireNonNull(caffeineManager.getCache(cacheName)).put(key, envelope);
    }

    private <T> void putL2(String cacheName, String key, CacheEnvelope<T> envelope){
        redisTemplate.opsForValue().set(cacheName + ":" + key, mapper.writeValueAsString(envelope),
                Duration.ofMinutes(cacheProperties.remote().ttlMin()));
    }

    private void evictL1(String cacheName, String key){
        Objects.requireNonNull(caffeineManager.getCache(cacheName)).evict(key);
    }

    private void evictL2(String cacheName, String key){
        redisTemplate.delete(cacheName + ":" + key);
    }

    private void evictAllL1(String cacheName){
        Objects.requireNonNull(caffeineManager.getCache(cacheName)).invalidate();
    }

    private void evictAllL2(String cacheName){
        Set<String> keys = redisTemplate.keys(cacheName + ":*");
        if(keys != null && !keys.isEmpty())
            redisTemplate.delete(keys);
    }
}
