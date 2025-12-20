package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 个人用户注册
     */
    @PostMapping("/register/personal")
    public Result<Map<String, Object>> registerPersonal(@RequestBody UserPersonal userPersonal) {
        Map<String, Object> result = authService.registerPersonal(userPersonal);
        return Result.success(result);
    }

    /**
     * 企业用户注册
     */
    @PostMapping("/register/enterprise")
    public Result<Map<String, Object>> registerEnterprise(@RequestBody UserEnterprise userEnterprise) {
        Map<String, Object> result = authService.registerEnterprise(userEnterprise);
        return Result.success(result);
    }

    /**
     * 个人用户登录
     */
    @PostMapping("/login/personal")
    public Result<Map<String, Object>> loginPersonal(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");
        Map<String, Object> result = authService.loginPersonal(username, password);
        return Result.success(result);
    }

    /**
     * 企业用户登录
     */
    @PostMapping("/login/enterprise")
    public Result<Map<String, Object>> loginEnterprise(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");
        Map<String, Object> result = authService.loginEnterprise(username, password);
        return Result.success(result);
    }

    /**
     * 忘记密码
     */
    @PostMapping("/forget-password")
    public Result<Boolean> forgetPassword(@RequestBody Map<String, Object> params) {
        String username = (String) params.get("username");
        String email = (String) params.get("email");
        Integer userType = (Integer) params.get("userType");
        boolean result = authService.forgetPassword(username, email, userType);
        return Result.success(result);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<Boolean> resetPassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String newPassword = params.get("newPassword");
        String token = params.get("token");
        boolean result = authService.resetPassword(username, newPassword, token);
        return Result.success(result);
    }
}
