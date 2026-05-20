package com.innowise.userservice.infrastructure.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CustomCaching {
    CustomCacheable[] cacheable() default {}; //NOTE: multiple @Cacheables are not supported: only the first one will be processed
    //I don't need to store values in multiple caches at once & it makes the BPP too complex
    CustomCachePut[] put() default {};
    CustomCacheEvict[] evict() default {};
}
