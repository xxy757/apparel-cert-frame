package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.service.AuthService;
import com.apparelcert.service.EmailService;
import com.apparelcert.service.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    @Autowired
    private EmailService emailService;

    /**
     * 个人用户注册
     */
    @PostMapping("/register/personal")
    public Result<Map<String, Object>> registerPersonal(@RequestBody UserPersonal userPersonal) {
        // 参数验证
        if (userPersonal.getUsername() == null || userPersonal.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (userPersonal.getPassword() == null || userPersonal.getPassword().trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }
        if (userPersonal.getEmail() == null || userPersonal.getEmail().trim().isEmpty()) {
            return Result.error(400, "邮箱不能为空");
        }
        
        Map<String, Object> result = authService.registerPersonal(userPersonal);
        Boolean success = (Boolean) result.get("success");
        
        if (success) {
            return Result.success(result);
        } else {
            String message = (String) result.get("message");
            return Result.error(400, message);
        }
    }

    /**
     * 企业用户注册
     */
    @PostMapping("/register/enterprise")
    public Result<Map<String, Object>> registerEnterprise(@RequestBody UserEnterprise userEnterprise) {
        // 参数验证
        if (userEnterprise.getUsername() == null || userEnterprise.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (userEnterprise.getPassword() == null || userEnterprise.getPassword().trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }
        if (userEnterprise.getEmail() == null || userEnterprise.getEmail().trim().isEmpty()) {
            return Result.error(400, "邮箱不能为空");
        }
        if (userEnterprise.getCompanyName() == null || userEnterprise.getCompanyName().trim().isEmpty()) {
            return Result.error(400, "企业名称不能为空");
        }
        
        Map<String, Object> result = authService.registerEnterprise(userEnterprise);
        Boolean success = (Boolean) result.get("success");
        
        if (success) {
            return Result.success(result);
        } else {
            String message = (String) result.get("message");
            return Result.error(400, message);
        }
    }

    /**
     * 个人用户登录
     */
    @PostMapping("/login/personal")
    public Result<Map<String, Object>> loginPersonal(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");
        
        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }
        
        // 检查账号是否被锁定
        Map<String, Object> lockInfo = loginAttemptService.checkLocked(username, 1);
        if ((Boolean) lockInfo.get("isLocked")) {
            long remainingSeconds = (Long) lockInfo.get("remainingSeconds");
            return Result.error(423, "账号已被锁定，请" + remainingSeconds + "秒后重试");
        }
        
        Map<String, Object> result = authService.loginPersonal(username, password);
        Boolean success = (Boolean) result.get("success");
        
        if (success) {
            // 登录成功，重置失败次数
            loginAttemptService.resetAttempts(username, 1);
            return Result.success(result);
        } else {
            // 登录失败，记录失败次数
            int remainingAttempts = loginAttemptService.recordFailedAttempt(username, 1);
            String message = (String) result.get("message");
            if (remainingAttempts > 0) {
                message += "，剩余尝试次数：" + remainingAttempts;
            } else {
                message = "登录失败次数过多，账号已被锁定5分钟";
            }
            result.put("remainingAttempts", remainingAttempts);
            return Result.error(401, message);
        }
    }

    /**
     * 企业用户登录
     */
    @PostMapping("/login/enterprise")
    public Result<Map<String, Object>> loginEnterprise(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");

        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }

        // 检查账号是否被锁定
        Map<String, Object> lockInfo = loginAttemptService.checkLocked(username, 2);
        if ((Boolean) lockInfo.get("isLocked")) {
            long remainingSeconds = (Long) lockInfo.get("remainingSeconds");
            return Result.error(423, "账号已被锁定，请" + remainingSeconds + "秒后重试");
        }

        Map<String, Object> result = authService.loginEnterprise(username, password);
        Boolean success = (Boolean) result.get("success");

        if (success) {
            // 登录成功，重置失败次数
            loginAttemptService.resetAttempts(username, 2);
            return Result.success(result);
        } else {
            // 登录失败，记录失败次数
            int remainingAttempts = loginAttemptService.recordFailedAttempt(username, 2);
            String message = (String) result.get("message");
            if (remainingAttempts > 0) {
                message += "，剩余尝试次数：" + remainingAttempts;
            } else {
                message = "登录失败次数过多，账号已被锁定5分钟";
            }
            result.put("remainingAttempts", remainingAttempts);
            return Result.error(401, message);
        }
    }

    /**
     * 管理员用户登录
     */
    @PostMapping("/login/admin")
    public Result<Map<String, Object>> loginAdmin(@RequestBody Map<String, String> loginInfo) {
        log.info("=======管理员用户登录 /login/admin=========");
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");
        log.info("=======>username: " + username);
        log.info("=======>password: " + password);
        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }

        // 检查账号是否被锁定
        Map<String, Object> lockInfo = loginAttemptService.checkLocked(username, 3);
        if ((Boolean) lockInfo.get("isLocked")) {
            long remainingSeconds = (Long) lockInfo.get("remainingSeconds");
            return Result.error(423, "账号已被锁定，请" + remainingSeconds + "秒后重试");
        }

        Map<String, Object> result = authService.loginAdmin(username, password);
        Boolean success = (Boolean) result.get("success");

        if (success) {
            // 登录成功，重置失败次数
            loginAttemptService.resetAttempts(username, 3);
            return Result.success(result);
        } else {
            // 登录失败，记录失败次数
            int remainingAttempts = loginAttemptService.recordFailedAttempt(username, 3);
            String message = (String) result.get("message");
            if (remainingAttempts > 0) {
                message += "，剩余尝试次数：" + remainingAttempts;
            } else {
                message = "登录失败次数过多，账号已被锁定5分钟";
            }
            result.put("remainingAttempts", remainingAttempts);
            return Result.error(401, message);
        }
    }

    /**
     * 检查账号锁定状态
     */
    @GetMapping("/check-lock")
    public Result<Map<String, Object>> checkLockStatus(
            @RequestParam String username,
            @RequestParam Integer userType) {
        Map<String, Object> lockInfo = loginAttemptService.checkLocked(username, userType);
        int remainingAttempts = loginAttemptService.getRemainingAttempts(username, userType);
        lockInfo.put("remainingAttempts", remainingAttempts);
        return Result.success(lockInfo);
    }

    /**
     * 发送注册验证码
     */
    @PostMapping("/send-register-code")
    public Result<Map<String, Object>> sendRegisterCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        if (email == null || email.trim().isEmpty()) {
            return Result.error(400, "邮箱不能为空");
        }
        
        String code = emailService.generateVerifyCode();
        boolean sent = emailService.sendRegisterVerifyCode(email, code);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", sent);
        result.put("message", sent ? "验证码已发送" : "验证码发送失败");
        return sent ? Result.success(result) : Result.error(500, "验证码发送失败");
    }
    
    /**
     * 验证注册验证码
     */
    @PostMapping("/verify-register-code")
    public Result<Map<String, Object>> verifyRegisterCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        
        boolean valid = emailService.verifyCode(email, code, "register");
        
        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);
        result.put("message", valid ? "验证码正确" : "验证码错误或已过期");
        return valid ? Result.success(result) : Result.error(400, "验证码错误或已过期");
    }

    /**
     * 忘记密码 - 发送重置验证码
     */
    @PostMapping("/forget-password")
    public Result<Map<String, Object>> forgetPassword(@RequestBody Map<String, Object> params) {
        String email = (String) params.get("email");
        Integer userType = params.get("userType") != null ? 
            Integer.parseInt(params.get("userType").toString()) : 1;
        
        if (email == null || email.trim().isEmpty()) {
            return Result.error(400, "邮箱不能为空");
        }
        
        // 验证邮箱是否存在
        boolean emailExists = authService.checkEmailExists(email, userType);
        if (!emailExists) {
            return Result.error(400, "该邮箱未注册");
        }
        
        // 生成并发送验证码
        String code = emailService.generateVerifyCode();
        boolean sent = emailService.sendResetPasswordCode(email, code);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", sent);
        result.put("message", sent ? "验证码已发送到您的邮箱" : "验证码发送失败");
        return sent ? Result.success(result) : Result.error(500, "验证码发送失败");
    }
    
    /**
     * 验证重置密码验证码
     */
    @PostMapping("/verify-reset-code")
    public Result<Map<String, Object>> verifyResetCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        
        // 只验证不消费验证码
        boolean valid = emailService.verifyCode(email, code, "reset");
        
        // 如果验证成功，重新保存验证码供后续重置密码使用
        if (valid) {
            emailService.saveVerifyCode(email, code, "reset");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("valid", valid);
        result.put("message", valid ? "验证码正确" : "验证码错误或已过期");
        return valid ? Result.success(result) : Result.error(400, "验证码错误或已过期");
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public Result<Map<String, Object>> resetPassword(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");
        String userTypeStr = params.get("userType");
        Integer userType = userTypeStr != null ? Integer.parseInt(userTypeStr) : 1;
        
        if (email == null || newPassword == null) {
            return Result.error(400, "参数不完整");
        }
        
        // 验证验证码
        boolean codeValid = emailService.verifyCode(email, code, "reset");
        if (!codeValid) {
            return Result.error(400, "验证码错误或已过期");
        }
        
        // 重置密码
        boolean result = authService.resetPasswordByEmail(email, newPassword, userType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", result ? "密码重置成功" : "密码重置失败");
        return result ? Result.success(response) : Result.error(500, "密码重置失败");
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current-user")
    public Result<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // 从 Authorization header 中提取 token
            String token = authHeader.replace("Bearer ", "");
            Map<String, Object> result = authService.getCurrentUser(token);
            Boolean success = (Boolean) result.get("success");
            
            if (success) {
                return Result.success(result);
            } else {
                String message = (String) result.get("message");
                return Result.error(401, message);
            }
        } catch (Exception e) {
            return Result.error(401, "认证信息无效");
        }
    }
}
