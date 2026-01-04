package org.example.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class CacheLogger extends SimpleKeyGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CacheLogger.class);

    @Override
    public Object generate(Object target, Method method, Object... params) {
        Object key = super.generate(target, method, params);

        String cacheInfo = String.format(
                "🔍 [Cache] Method: %s, Key: %s, Params: %d",
                method.getName(),
                key.toString(),
                params.length
        );

        logger.info(cacheInfo);
        return key;
    }
}