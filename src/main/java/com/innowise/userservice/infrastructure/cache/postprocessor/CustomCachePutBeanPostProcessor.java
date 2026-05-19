package com.innowise.userservice.infrastructure.cache.postprocessor;

import com.innowise.userservice.infrastructure.cache.TwoLevelCacheService;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCachePut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomCachePutBeanPostProcessor implements BeanPostProcessor {

    private Map<String, Class<?>> map = new HashMap<>();
    private final TwoLevelCacheService cacheService;

    @Override
    public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for(Method method : methods){
            if(method.isAnnotationPresent(CustomCachePut.class)){
                map.put(beanName, bean.getClass());
            }
        }
        return bean;
    }

    @Override
    public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        final Class<?> beanClass = map.get(beanName);
        if(beanClass != null) {
            return Proxy.newProxyInstance(
                    beanClass.getClassLoader(),
                    beanClass.getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            Object result = method.invoke(bean, args);
                            CustomCachePut annotation = getAnnotation(method, beanClass);
                            if (annotation != null && cacheService.isCachingOn()) {
                                cacheService.put(annotation.cacheName(), constructKey(annotation, args), result);
                            }
                            return result;
                        }
                    }
            );
        }
        return bean;
    }

    private CustomCachePut getAnnotation(Method method, Class<?> beanClass){
        CustomCachePut annotation = method.getAnnotation(CustomCachePut.class);
        if(annotation != null)
            return annotation;
        method = AopUtils.getMostSpecificMethod(method, beanClass);
        return method.getAnnotation(CustomCachePut.class);
    }

    private String constructKey(CustomCachePut annotation, Object[] args){
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
