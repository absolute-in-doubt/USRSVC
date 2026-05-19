package com.innowise.userservice.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
@RequiredArgsConstructor
public class CacheConfig {

    public static final String PAYMENT_CARDS_CACHE = "payment_cards_cache";
    public static final String USERS_CACHE = "users_cache";

    private final CacheProperties cacheProperties;

    @Bean
    public CacheManager cacheManager() {
        int maxSize = cacheProperties.local().maxSize();
        int expirationMin = cacheProperties.local().expirationMin();

        Caffeine<Object, Object> users = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(Duration.ofMinutes(expirationMin))
                .recordStats();

        Caffeine<Object, Object> paymentCards = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(Duration.ofMinutes(expirationMin))
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
