package com.apparelcert.service;

import java.util.Map;

/**
 * 登录尝试服务接口
 * 用于管理登录失败次数限制和账号锁定
 */
public interface LoginAttemptService {

    /**
     * 最大失败次数
     */
    int MAX_ATTEMPTS = 5;
    
    /**
     * 锁定时间（秒）
     */
    int LOCK_TIME_SECONDS = 300;

    /**
     * 检查账号是否被锁定
     * @param username 用户名
     * @param userType 用户类型
     * @return 锁定信息，包含isLocked和remainingSeconds
     */
    Map<String, Object> checkLocked(String username, Integer userType);

    /**
     * 记录登录失败
     * @param username 用户名
     * @param userType 用户类型
     * @return 剩余尝试次数
     */
    int recordFailedAttempt(String username, Integer userType);

    /**
     * 登录成功后重置失败次数
     * @param username 用户名
     * @param userType 用户类型
     */
    void resetAttempts(String username, Integer userType);

    /**
     * 获取剩余尝试次数
     * @param username 用户名
     * @param userType 用户类型
     * @return 剩余尝试次数
     */
    int getRemainingAttempts(String username, Integer userType);
}
