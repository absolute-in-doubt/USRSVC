package com.innowise.userservice.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
@RequiredArgsConstructor
public class CacheConfig {

    public static final String PAYMENT_CARDS_CACHE = "payment_cards_cache";
    public static final String PAYMENT_CARDS_VIA_USER_ID_CACHE = "pc_via_user_id_cache";
    public static final String PAYMENT_CARDS_FILTERED_AND_PAGED_CACHE = "pc_filtered_and_paged_cache";
    public static final String USERS_CACHE = "users_cache";
    public static final String USERS_FILTERED_AND_PAGED_CACHE = "users_filtered_and_paged_cache";
    private static final String[] caches = {
            PAYMENT_CARDS_CACHE,
            PAYMENT_CARDS_VIA_USER_ID_CACHE,
            PAYMENT_CARDS_FILTERED_AND_PAGED_CACHE,
            USERS_CACHE,
            USERS_FILTERED_AND_PAGED_CACHE
    };

    private final CacheProperties cacheProperties;

    @Bean
    public CacheManager cacheManager() {
        int maxSize = cacheProperties.local().maxSize();
        int expirationMin = cacheProperties.local().expirationMin();

        CaffeineCacheManager cacheManager = new CaffeineCacheManager(caches);
        for(int i = 0; i < caches.length; i++) {
            Caffeine<Object, Object> cache = Caffeine.newBuilder()
                    .maximumSize(maxSize)
                    .expireAfterAccess(Duration.ofMinutes(expirationMin))
                    .recordStats();
            cacheManager.registerCustomCache(caches[i], cache.build());
        }
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
