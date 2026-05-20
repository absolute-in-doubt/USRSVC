package com.innowise.userservice.infrastructure.cache.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CustomCacheEvict {
    String cacheName();
    int[] keyArgumentIndexes() default {};
    boolean allEntries() default false;
}
