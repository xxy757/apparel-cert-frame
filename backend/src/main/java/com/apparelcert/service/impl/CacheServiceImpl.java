package com.apparelcert.service.impl;

import com.apparelcert.service.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务实现类
 */
@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void set(String key, Object value) {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception e) {
            logger.error("Redis set error: key={}, error={}", key, e.getMessage());
        }
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            }
        } catch (Exception e) {
            logger.error("Redis set error: key={}, error={}", key, e.getMessage());
        }
    }

    @Override
    public Object get(String key) {
        try {
            if (redisTemplate != null) {
                return redisTemplate.opsForValue().get(key);
            }
        } catch (Exception e) {
            logger.error("Redis get error: key={}, error={}", key, e.getMessage());
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = get(key);
            if (value == null) {
                return null;
            }
            if (clazz.isInstance(value)) {
                return (T) value;
            }
            // 尝试转换
            return objectMapper.convertValue(value, clazz);
        } catch (Exception e) {
            logger.error("Redis get error: key={}, error={}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public boolean delete(String key) {
        try {
            if (redisTemplate != null) {
                return Boolean.TRUE.equals(redisTemplate.delete(key));
            }
        } catch (Exception e) {
            logger.error("Redis delete error: key={}, error={}", key, e.getMessage());
        }
        return false;
    }

    @Override
    public boolean hasKey(String key) {
        try {
            if (redisTemplate != null) {
                return Boolean.TRUE.equals(redisTemplate.hasKey(key));
            }
        } catch (Exception e) {
            logger.error("Redis hasKey error: key={}, error={}", key, e.getMessage());
        }
        return false;
    }

    @Override
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            if (redisTemplate != null) {
                return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
            }
        } catch (Exception e) {
            logger.error("Redis expire error: key={}, error={}", key, e.getMessage());
        }
        return false;
    }

    @Override
    public long getExpire(String key) {
        try {
            if (redisTemplate != null) {
                Long expire = redisTemplate.getExpire(key);
                return expire != null ? expire : -1;
            }
        } catch (Exception e) {
            logger.error("Redis getExpire error: key={}, error={}", key, e.getMessage());
        }
        return -1;
    }

    @Override
    public long increment(String key, long delta) {
        try {
            if (redisTemplate != null) {
                Long result = redisTemplate.opsForValue().increment(key, delta);
                return result != null ? result : 0;
            }
        } catch (Exception e) {
            logger.error("Redis increment error: key={}, error={}", key, e.getMessage());
        }
        return 0;
    }

    @Override
    public long decrement(String key, long delta) {
        try {
            if (redisTemplate != null) {
                Long result = redisTemplate.opsForValue().decrement(key, delta);
                return result != null ? result : 0;
            }
        } catch (Exception e) {
            logger.error("Redis decrement error: key={}, error={}", key, e.getMessage());
        }
        return 0;
    }
}
