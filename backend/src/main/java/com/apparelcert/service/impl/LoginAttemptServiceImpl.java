package com.apparelcert.service.impl;

import com.apparelcert.entity.LoginAttempt;
import com.apparelcert.mapper.LoginAttemptMapper;
import com.apparelcert.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录尝试服务实现类
 */
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    @Autowired
    private LoginAttemptMapper loginAttemptMapper;

    @Override
    public Map<String, Object> checkLocked(String username, Integer userType) {
        Map<String, Object> result = new HashMap<>();
        LoginAttempt attempt = loginAttemptMapper.findByUsernameAndType(username, userType);
        
        if (attempt == null || attempt.getLockTime() == null) {
            result.put("isLocked", false);
            result.put("remainingSeconds", 0);
            return result;
        }
        
        // 检查锁定是否过期
        long lockEndTime = attempt.getLockTime().getTime() + (LOCK_TIME_SECONDS * 1000L);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime < lockEndTime) {
            result.put("isLocked", true);
            result.put("remainingSeconds", (lockEndTime - currentTime) / 1000);
        } else {
            // 锁定已过期，重置
            attempt.setFailCount(0);
            attempt.setLockTime(null);
            attempt.setUpdateTime(new Date());
            loginAttemptMapper.updateById(attempt);
            result.put("isLocked", false);
            result.put("remainingSeconds", 0);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int recordFailedAttempt(String username, Integer userType) {
        LoginAttempt attempt = loginAttemptMapper.findByUsernameAndType(username, userType);
        Date now = new Date();
        
        if (attempt == null) {
            // 创建新记录
            attempt = new LoginAttempt();
            attempt.setUsername(username);
            attempt.setUserType(userType);
            attempt.setFailCount(1);
            attempt.setLastAttemptTime(now);
            attempt.setCreateTime(now);
            attempt.setUpdateTime(now);
            loginAttemptMapper.insert(attempt);
            return MAX_ATTEMPTS - 1;
        }
        
        // 检查是否需要重置（锁定已过期）
        if (attempt.getLockTime() != null) {
            long lockEndTime = attempt.getLockTime().getTime() + (LOCK_TIME_SECONDS * 1000L);
            if (System.currentTimeMillis() >= lockEndTime) {
                attempt.setFailCount(0);
                attempt.setLockTime(null);
            }
        }
        
        // 增加失败次数
        int newFailCount = attempt.getFailCount() + 1;
        attempt.setFailCount(newFailCount);
        attempt.setLastAttemptTime(now);
        attempt.setUpdateTime(now);
        
        // 达到最大次数，锁定账号
        if (newFailCount >= MAX_ATTEMPTS) {
            attempt.setLockTime(now);
        }
        
        loginAttemptMapper.updateById(attempt);
        
        return Math.max(0, MAX_ATTEMPTS - newFailCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetAttempts(String username, Integer userType) {
        LoginAttempt attempt = loginAttemptMapper.findByUsernameAndType(username, userType);
        if (attempt != null) {
            attempt.setFailCount(0);
            attempt.setLockTime(null);
            attempt.setUpdateTime(new Date());
            loginAttemptMapper.updateById(attempt);
        }
    }

    @Override
    public int getRemainingAttempts(String username, Integer userType) {
        LoginAttempt attempt = loginAttemptMapper.findByUsernameAndType(username, userType);
        if (attempt == null) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - attempt.getFailCount());
    }
}
