package com.apparelcert.service.impl;

import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.service.AuthService;
import com.apparelcert.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Map<String, Object> registerPersonal(UserPersonal userPersonal) {
        // TODO: 实现个人用户注册逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "注册成功");
        return result;
    }

    @Override
    public Map<String, Object> registerEnterprise(UserEnterprise userEnterprise) {
        // TODO: 实现企业用户注册逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "注册成功，待审核");
        return result;
    }

    @Override
    public Map<String, Object> loginPersonal(String username, String password) {
        // TODO: 实现个人用户登录逻辑
        Map<String, Object> result = new HashMap<>();
        // 模拟登录成功
        Long userId = 1L;
        String token = jwtUtil.generateToken(userId, 1);
        result.put("success", true);
        result.put("token", token);
        result.put("userId", userId);
        result.put("userType", 1);
        result.put("message", "登录成功");
        return result;
    }

    @Override
    public Map<String, Object> loginEnterprise(String username, String password) {
        // TODO: 实现企业用户登录逻辑
        Map<String, Object> result = new HashMap<>();
        // 模拟登录成功
        Long userId = 1L;
        String token = jwtUtil.generateToken(userId, 2);
        result.put("success", true);
        result.put("token", token);
        result.put("userId", userId);
        result.put("userType", 2);
        result.put("message", "登录成功");
        return result;
    }

    @Override
    public boolean forgetPassword(String username, String email, Integer userType) {
        // TODO: 实现忘记密码逻辑
        return true;
    }

    @Override
    public boolean resetPassword(String username, String newPassword, String token) {
        // TODO: 实现重置密码逻辑
        return true;
    }
}
