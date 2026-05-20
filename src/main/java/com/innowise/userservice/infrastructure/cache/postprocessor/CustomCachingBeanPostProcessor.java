package com.innowise.userservice.infrastructure.cache.postprocessor;

import com.innowise.userservice.infrastructure.cache.TwoLevelCacheService;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCacheEvict;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCachePut;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCacheable;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCaching;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

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
public class CustomCachingBeanPostProcessor implements BeanPostProcessor {

    private Map<String, Class<?>> map = new HashMap<>();
    private final TwoLevelCacheService cacheService;
    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final Map<String, Expression> expressionsCache = new HashMap<>();

    @Override
    public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for(Method method : methods){
            if(method.isAnnotationPresent(CustomCaching.class)){
                map.put(beanName, bean.getClass());
            }
        }
        return bean;
    }

    @Override
    public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        final Class<?> beanClass = map.get(beanName);

        if(beanClass != null) {
            ProxyFactory proxyFactory = new ProxyFactory(bean);
            proxyFactory.addAdvice((MethodInterceptor) invocation -> {
                        Method method = invocation.getMethod();
                        Object[] args = invocation.getArguments();

                        CustomCaching annotation = getAnnotation(method, beanClass);
                        Object result;
                        if(annotation != null) {
                            if(annotation.cacheable().length == 0){
                                result = invocation.proceed();
                            } else {
                                CustomCacheable cacheable = annotation.cacheable()[0];
                                result = cacheService.get(cacheable.cacheName(),
                                        constructKey(invocation.getArguments(), cacheable.keyArgumentIndexes()),
                                        method.getGenericReturnType(),
                                        () -> {
                                            try {
                                                return invocation.proceed();
                                            } catch (Throwable e) {
                                                Logger log = LoggerFactory.getLogger(beanClass);
                                                log.error("Error during {} cacheable method execution", method.getName(), e);
                                                throw new RuntimeException(e);
                                            }
                                        });
                                }
                            for(CustomCachePut put : annotation.put()){
                                cacheService.put(put.cacheName(), constructKeyWithSpEL(put, args, result), result);
                            }
                            for(CustomCacheEvict evict : annotation.evict())
                            {
                                if(evict.allEntries())
                                    cacheService.evictAll(evict.cacheName());
                                else
                                    cacheService.evict(evict.cacheName(), constructKey(args, evict.keyArgumentIndexes()));
                            }
                            return result;
                        } else
                            return invocation.proceed();
                    }
            );
            return proxyFactory.getProxy(beanClass.getClassLoader());
        }
        return bean;
    }

    private CustomCaching getAnnotation(Method method, Class<?> beanClass){
        CustomCaching annotation = method.getAnnotation(CustomCaching.class);
        if(annotation != null)
            return annotation;
        method = AopUtils.getMostSpecificMethod(method, beanClass);
        return method.getAnnotation(CustomCaching.class);
    }

    private String constructKey(Object[] args, int[] keyArgumentIndexes){
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

    private String constructKeyWithSpEL(CustomCachePut annotation, Object[] args, Object result){
        //It wasn't supposed to go that far
        //Mainly it's used to retrieve the key from the returned value
        log.debug("Constructing key for @CachePut with SpEL: {}", annotation.keySpEL());
        if(!annotation.keySpEL().isEmpty()){
            Expression expression = expressionsCache.computeIfAbsent(annotation.keySpEL(), spelParser::parseExpression);
            StandardEvaluationContext expressionContext = new StandardEvaluationContext();
            for(int i = 0; i < args.length; i++){
                expressionContext.setVariable("a" + i, args[i]);
            }
            expressionContext.setVariable("result", result);
            Object rawKey = expression.getValue(expressionContext); //supposed to return id of the return value
            log.debug("Evaluating SpEL for @CachePut: raw key = {}", rawKey);
            if(rawKey == null) {
                throw new IllegalStateException("Something is wrong with the SpEL processing in the CustomCachePut BPP");
            }
            return rawKey.toString();
        }


        int[] keyArgumentIndexes = annotation.keyArgumentIndexes();
        StringBuilder key = new StringBuilder();
        try {
            for(int i : keyArgumentIndexes){
                key.append(args[i].hashCode());
            }
        } catch(IndexOutOfBoundsException e) {
            log.error("key argument index out of bounds for @CustomCacheable annotation");
            throw e;
        }
        return key.toString();
    }
}
