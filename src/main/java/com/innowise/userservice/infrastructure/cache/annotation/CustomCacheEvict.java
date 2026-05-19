package com.innowise.userservice.infrastructure.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CustomCacheEvict {
    String cacheName();
    int[] keyArgumentIndexes();
    boolean allEntries() default false;
}
