package com.innowise.userservice.infrastructure.profiling;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@ConditionalOnProperty(
        name = "application.profiling.enabled",
        havingValue = "true"
)
public class ProfilingAspect {

    @Around("@annotation(Profiling)")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();

            return result;
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} executed in {} ms", pjp.getSignature().toShortString(), duration);
        }
    }
}
