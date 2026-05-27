package com.innowise.userservice.infrastructure.profiling.postprocessor;

import com.innowise.userservice.infrastructure.cache.annotation.CustomCacheable;
import com.innowise.userservice.infrastructure.profiling.annotation.Profiling;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "application.profiling.enabled",
        havingValue = "true"
)
public class ProfilingPostProcessor implements BeanPostProcessor {
    Map<String, Class<?>> map = new HashMap<>();

    @Override
    public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for(Method method : methods){
            if(method.isAnnotationPresent(Profiling.class)){
                log.trace("Profiling method {} found in bean {}", method.getName(), beanName);
                map.put(beanName, bean.getClass());
            }
        }
        return bean;
    }

    @Override
    public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = map.get(beanName);
        if(beanClass != null){
            ProxyFactory proxyFactory = new ProxyFactory(bean);
            proxyFactory.addAdvice( (MethodInterceptor) invocation -> {
                Method method = invocation.getMethod();

                Profiling annotation = getAnnotation(method, beanClass);
                if(annotation != null){
                    long start = System.nanoTime();
                    Object result = method.invoke(bean, invocation.getArguments());
                    long duration = System.nanoTime() - start;
                    log.info("{} executed in {}.{} ms",
                                invocation.getMethod().getName(),
                                TimeUnit.NANOSECONDS.toMillis(duration),
                                TimeUnit.NANOSECONDS.toMicros(duration) % 1000);

                    return result;
                }
                return invocation.proceed();
            });
            return proxyFactory.getProxy(beanClass.getClassLoader());
        }

        return bean;
    }

    private Profiling getAnnotation(Method method, Class<?> beanClass){
        Profiling annotation = method.getAnnotation(Profiling.class);
        if(annotation != null)
            return annotation;
        method = AopUtils.getMostSpecificMethod(method, beanClass);
        return method.getAnnotation(Profiling.class);
    }
}
