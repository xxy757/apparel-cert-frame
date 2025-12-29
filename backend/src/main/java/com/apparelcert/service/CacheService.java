package com.apparelcert.service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 */
public interface CacheService {
    
    /**
     * 设置缓存
     */
    void set(String key, Object value);
    
    /**
     * 设置缓存（带过期时间）
     */
    void set(String key, Object value, long timeout, TimeUnit unit);
    
    /**
     * 获取缓存
     */
    Object get(String key);
    
    /**
     * 获取缓存（指定类型）
     */
    <T> T get(String key, Class<T> clazz);
    
    /**
     * 删除缓存
     */
    boolean delete(String key);
    
    /**
     * 判断key是否存在
     */
    boolean hasKey(String key);
    
    /**
     * 设置过期时间
     */
    boolean expire(String key, long timeout, TimeUnit unit);
    
    /**
     * 获取过期时间
     */
    long getExpire(String key);
    
    /**
     * 递增
     */
    long increment(String key, long delta);
    
    /**
     * 递减
     */
    long decrement(String key, long delta);
}
