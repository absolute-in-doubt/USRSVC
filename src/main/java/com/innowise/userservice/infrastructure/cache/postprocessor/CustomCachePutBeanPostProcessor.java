package com.innowise.userservice.infrastructure.cache.postprocessor;

import com.innowise.userservice.infrastructure.cache.TwoLevelCacheService;
import com.innowise.userservice.infrastructure.cache.annotation.CustomCachePut;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.jspecify.annotations.Nullable;
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "application.caching.enabled",
        havingValue = "true"
)
public class CustomCachePutBeanPostProcessor implements BeanPostProcessor {

    private Map<String, Class<?>> map = new HashMap<>();
    private final TwoLevelCacheService cacheService;
    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final Map<String, Expression> expressionsCache = new HashMap<>();

    @Override
    public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
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
            ProxyFactory proxyFactory = new ProxyFactory(bean);
            proxyFactory.addAdvice((MethodInterceptor) invocation -> {
                    Method method = invocation.getMethod();
                    Object[] args = invocation.getArguments();

                    Object result = invocation.proceed();
                    CustomCachePut annotation = getAnnotation(method, beanClass);
                    if (annotation != null) {
                        cacheService.put(annotation.cacheName(), constructKey(annotation, args, result), result);
                    }
                    return result;

                }
            );
            return proxyFactory.getProxy(beanClass.getClassLoader());
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

    private String constructKey(CustomCachePut annotation, Object[] args, Object result){
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
