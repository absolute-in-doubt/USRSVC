package com.innowise.userservice.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;

@Configuration
public class CacheConfig {

    //TODO move it all away to the constants class
    public static final String PAYMENT_CARDS_CACHE = "payment_cards_cache";
    public static final String USERS_CACHE = "users_cache";
    public static final int LOCAL_CACHE_MAX_SIZE = 1000;
    public static final int LOCAL_CACHE_EXPIRATION_MIN = 1;

    public static final int REMOTE_CACHE_STALE_AFTER_MIN = 10;
    public static final int REMOTE_CACHE_TTL_MIN = 15;

    @Bean
    public CacheManager cacheManager() {

        Caffeine<Object, Object> users = Caffeine.newBuilder()
                .maximumSize(LOCAL_CACHE_MAX_SIZE)
                .expireAfterAccess(Duration.ofMinutes(LOCAL_CACHE_EXPIRATION_MIN))
                .recordStats();

        Caffeine<Object, Object> paymentCards = Caffeine.newBuilder()
                .maximumSize(LOCAL_CACHE_MAX_SIZE)
                .expireAfterAccess(Duration.ofMinutes(LOCAL_CACHE_EXPIRATION_MIN))
                .recordStats();

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(USERS_CACHE, PAYMENT_CARDS_CACHE);
        cacheManager.setCaffeine(users);
        cacheManager.setCaffeine(paymentCards);
        return cacheManager;

    }


    @Bean
    public ThreadPoolTaskExecutor cacheRefreshExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setBeanName("cacheRefreshExecutor");
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-cache-refresh-");
        executor.initialize();
        return executor;
    }

}
