package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.config.ResetPwdProperties;
import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.service.AuthService;
import com.apparelcert.service.EmailService;
import com.apparelcert.service.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private ResetPwdProperties resetPwdProperties;

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

    /**
     * 开发模式重置密码（仅供开发/演示环境使用）
     * 需要配置 app.resetPwd.devMode=true 且来源IP在允许范围内
     */
    @PostMapping("/dev-reset-password")
    public Result<Map<String, Object>> devResetPassword(
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {

        // 检查是否启用开发模式
        if (!resetPwdProperties.isDevMode()) {
            log.warn("开发模式重置密码功能未启用，尝试访问来自: {}", getClientIp(request));
            return Result.error(403, "开发模式重置密码功能未启用");
        }

        // 检查来源IP是否在允许范围内
        String clientIp = getClientIp(request);
        if (!isIpAllowed(clientIp)) {
            log.warn("开发模式重置密码访问被拒绝，来源IP不在允许范围内: {}", clientIp);
            return Result.error(403, "访问被拒绝：来源IP不在允许范围内");
        }

        String account = params.get("account");
        String newPassword = params.get("newPassword");

        // 参数验证
        if (account == null || account.trim().isEmpty()) {
            return Result.error(400, "账号不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error(400, "新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.error(400, "密码长度不能少于6位");
        }

        log.info("开发模式重置密码请求 - 账号: {}, 来源IP: {}", account, clientIp);

        // 执行重置
        Map<String, Object> result = authService.devResetPassword(account, newPassword);
        Boolean success = (Boolean) result.get("success");

        if (success) {
            log.info("开发模式重置密码成功 - 账号: {}, 用户类型: {}", account, result.get("userType"));
            return Result.success(result);
        } else {
            log.warn("开发模式重置密码失败 - 账号: {}, 原因: {}", account, result.get("message"));
            return Result.error(400, (String) result.get("message"));
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况（X-Forwarded-For可能包含多个IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 检查IP是否在允许的CIDR范围内
     */
    private boolean isIpAllowed(String clientIp) {
        List<String> allowCidrs = resetPwdProperties.getAllowCidrs();
        if (allowCidrs == null || allowCidrs.isEmpty()) {
            return false;
        }

        try {
            // 处理IPv6地址的缩写格式（如 ::1）
            String normalizedIp = normalizeIp(clientIp);

            for (String cidr : allowCidrs) {
                if (isIpInCidr(normalizedIp, cidr)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("IP检查异常: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 规范化IP地址（处理IPv6缩写）
     */
    private String normalizeIp(String ip) {
        if (ip == null) {
            return "";
        }
        // 移除可能的端口
        if (ip.contains(":") && ip.contains(".") && ip.indexOf(":") < ip.indexOf(".")) {
            // IPv6 with IPv4 embedded, remove port if exists
            int bracketEnd = ip.lastIndexOf(']');
            if (bracketEnd > 0 && ip.length() > bracketEnd + 1 && ip.charAt(bracketEnd + 1) == ':') {
                // [IPv6]:port format
                ip = ip.substring(1, bracketEnd);
            }
        } else if (ip.contains(":") && !ip.startsWith("[")) {
            // 可能包含端口的IPv6或普通IPv6
            int lastColon = ip.lastIndexOf(':');
            if (lastColon > 0 && ip.substring(lastColon + 1).matches("\\d+")) {
                ip = ip.substring(0, lastColon);
            }
        } else if (ip.contains(":")) {
            // 普通IPv4:port
            ip = ip.substring(0, ip.indexOf(':'));
        }

        // 处理 ::1 这种IPv6缩写
        if ("::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "::1";
        }
        if ("127.0.0.1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

    /**
     * 检查IP是否在CIDR范围内
     */
    private boolean isIpInCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String networkIp = parts[0];
            int prefixLength = parts.length > 1 ? Integer.parseInt(parts[1]) :
                (networkIp.contains(":") ? 128 : 32);

            // 处理 ::1/128 和 127.0.0.1/32 的精确匹配
            if (prefixLength == (networkIp.contains(":") ? 128 : 32)) {
                return normalizeIp(ip).equals(normalizeIp(networkIp));
            }

            // 简单的CIDR匹配（仅支持 /8, /16, /24, /32 等标准前缀）
            if (ip.contains(".") && networkIp.contains(".")) {
                return isIpv4InCidr(ip, networkIp, prefixLength);
            }

            // 对于IPv6 CIDR，做简单的前缀匹配
            if (ip.contains(":") && networkIp.contains(":")) {
                return isIpv6InCidr(ip, networkIp, prefixLength);
            }

        } catch (Exception e) {
            log.error("CIDR匹配异常: ip={}, cidr={}, error={}", ip, cidr, e.getMessage());
        }
        return false;
    }

    /**
     * IPv4 CIDR匹配
     */
    private boolean isIpv4InCidr(String ip, String networkIp, int prefixLength) {
        String[] ipParts = ip.split("\\.");
        String[] networkParts = networkIp.split("\\.");

        if (ipParts.length != 4 || networkParts.length != 4) {
            return false;
        }

        int fullPrefixBytes = prefixLength / 8;
        int partialPrefixBits = prefixLength % 8;

        // 比较完整字节
        for (int i = 0; i < fullPrefixBytes; i++) {
            if (!ipParts[i].equals(networkParts[i])) {
                return false;
            }
        }

        // 比较部分字节
        if (partialPrefixBits > 0 && fullPrefixBytes < 4) {
            int ipByte = Integer.parseInt(ipParts[fullPrefixBytes]);
            int networkByte = Integer.parseInt(networkParts[fullPrefixBytes]);
            int mask = 256 - (1 << (8 - partialPrefixBits));
            if ((ipByte & mask) != (networkByte & mask)) {
                return false;
            }
        }

        return true;
    }

    /**
     * IPv6 CIDR匹配（简化版，主要处理 ::1 和本地地址）
     */
    private boolean isIpv6InCidr(String ip, String networkIp, int prefixLength) {
        // 规范化IPv6地址
        String normalizedIp = normalizeIp(ip);
        String normalizedNetwork = normalizeIp(networkIp);

        // 对于 /128 的精确匹配
        if (prefixLength == 128) {
            return normalizedIp.equals(normalizedNetwork);
        }

        // 对于 /64 的前缀匹配
        if (prefixLength == 64) {
            String[] ipParts = normalizedIp.split(":");
            String[] networkParts = normalizedNetwork.split(":");

            if (ipParts.length >= 8 && networkParts.length >= 8) {
                // 比较前4段（64位）
                for (int i = 0; i < 4; i++) {
                    if (!ipParts[i].equals(networkParts[i])) {
                        return false;
                    }
                }
                return true;
            }
        }

        // 对于 ::1 的特殊处理
        if ("::1".equals(normalizedNetwork)) {
            return "::1".equals(normalizedIp);
        }

        return false;
    }
}
