package com.innowise.userservice.infrastructure.cache.postprocessor;

import com.innowise.userservice.domain.model.exception.AccessDeniedException;
import com.innowise.userservice.domain.model.exception.PaymentCardNotFoundException;
import com.innowise.userservice.infrastructure.cache.TwoLevelCacheService;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCacheable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "application.caching.enabled",
        havingValue = "true"
)
public class CustomCacheableBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, Class<?>> map = new HashMap<>();
    private final TwoLevelCacheService cacheService;

    @Override
    public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for(Method method : methods){
            if(method.isAnnotationPresent(CustomCacheable.class))
                map.put(beanName, bean.getClass());
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
                        CustomCacheable annotation = getAnnotation(method, beanClass);
                        if(annotation != null) {
                            return cacheService.get(annotation.cacheName(), constructKey(annotation, invocation.getArguments()), method.getGenericReturnType(),
                                     () -> {
                                         try {
                                             return invocation.proceed();
                                         } catch (PaymentCardNotFoundException | AccessDeniedException e){
                                              throw e;
                                         }
                                         catch (Throwable e) {
                                             Logger log = LoggerFactory.getLogger(beanClass);
                                             log.error("Error during {} cacheable method execution", method.getName(), e);
                                             throw new RuntimeException(e);
                                         }
                                     });
                        } else
                            return invocation.proceed();
                    }
                );
            return proxyFactory.getProxy(beanClass.getClassLoader());
        }
        return bean;
    }

    private CustomCacheable getAnnotation(Method method, Class<?> beanClass){
        CustomCacheable annotation = method.getAnnotation(CustomCacheable.class);
        if(annotation != null)
            return annotation;
        method = AopUtils.getMostSpecificMethod(method, beanClass);
        return method.getAnnotation(CustomCacheable.class);
    }

    private String constructKey(CustomCacheable annotation, Object[] args){
        int[] keyArgumentIndexes = annotation.keyArgumentIndexes();
        StringBuilder key = new StringBuilder();

        try {
            for (int i : keyArgumentIndexes) {
                key.append(args[i].hashCode());
            }
        } catch(IndexOutOfBoundsException e) {
            log.error("key argument index out of bounds for @CustomCacheable annotation");
            throw e;
        }

        log.debug("Constructing key for @CustomCacheable: {}", key.toString());
        return key.toString();
    }
}
