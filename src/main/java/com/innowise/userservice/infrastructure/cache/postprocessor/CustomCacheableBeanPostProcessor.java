package com.innowise.userservice.infrastructure.cache.postprocessor;

import com.innowise.userservice.infrastructure.cache.TwoLevelCacheService;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCacheable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomCacheableBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, Class<?>> map = new HashMap<>();
    private final TwoLevelCacheService cacheService;

    @Override
    public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = bean.getClass().getDeclaredMethods();
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
            return Proxy.newProxyInstance(
                    beanClass.getClassLoader(),
                    beanClass.getInterfaces(),
                    (proxy, method, args) -> {

                        CustomCacheable annotation = getAnnotation(method, beanClass);
                        if(annotation != null && cacheService.isCachingOn()) {
                            return cacheService.get(annotation.cacheName(), constructKey(annotation, args), method.getGenericReturnType(),
                                     () -> {
                                         try {
                                             return method.invoke(bean, args);
                                         } catch (IllegalAccessException | InvocationTargetException e) {
                                             Logger log = LoggerFactory.getLogger(beanClass);
                                             log.error("Error during {} cacheable method execution", method.getName(), e);
                                             throw new RuntimeException(e);
                                         }
                                     });
                        } else
                            return method.invoke(bean, args);
                    }
                );
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
                key.append(args[i].hashCode()).append(" ");
            }
        } catch(IndexOutOfBoundsException e) {
            log.error("key argument index out of bounds for @CustomCacheable annotation");
            throw e;
        }

        return key.toString();
    }
}
