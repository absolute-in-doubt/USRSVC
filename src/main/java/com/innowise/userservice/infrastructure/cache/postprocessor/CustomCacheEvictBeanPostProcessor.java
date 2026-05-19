package com.innowise.userservice.infrastructure.cache.postprocessor;

import com.innowise.userservice.infrastructure.cache.TwoLevelCacheService;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCacheEvict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomCacheEvictBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, Class<?>> map = new HashMap<>();
    private final TwoLevelCacheService cacheService;

    @Override
    public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for(Method method : methods){
            if(method.isAnnotationPresent(CustomCacheEvict.class))
                map.put(beanName, bean.getClass());
        }
        return bean;
    }

    @Override
    public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = map.get(beanName);
        if(beanClass != null){
            return Proxy.newProxyInstance(
                    beanClass.getClassLoader(),
                    beanClass.getInterfaces(),
                    (proxy, method, args) -> {
                        Object result = method.invoke(bean, args);
                        CustomCacheEvict annotation = getAnnotation(method, beanClass);
                        if(annotation != null && cacheService.isCachingOn()){
                            if(annotation.allEntries())
                                cacheService.evictAll(annotation.cacheName());
                            else
                                cacheService.evict(annotation.cacheName(), constructKey(annotation, args));
                        }
                        return result;
                    }
                );
        }
        return bean;
    }

    private CustomCacheEvict getAnnotation(Method method, Class<?> beanClass){
        CustomCacheEvict annotation = method.getAnnotation(CustomCacheEvict.class);
        if(annotation != null)
            return annotation;
        method = AopUtils.getMostSpecificMethod(method, beanClass);
        return method.getAnnotation(CustomCacheEvict.class);
    }

    private String constructKey(CustomCacheEvict annotation, Object[] args){
        int[] keyArgumentIndexes = annotation.keyArgumentIndexes();
        StringBuilder key = new StringBuilder();
        try {
            for(int i : keyArgumentIndexes){
                key.append(args[i].hashCode()).append(" ");
            }
        } catch(IndexOutOfBoundsException e) {
            log.error("key argument index out of bounds for @CustomCacheable annotation");
            throw e;
        }
        return key.toString();
    }
}
