package com.apparelcert.service.impl;

import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.mapper.UserEnterpriseMapper;
import com.apparelcert.mapper.UserPersonalMapper;
import com.apparelcert.service.AuthService;
import com.apparelcert.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserPersonalMapper userPersonalMapper;

    @Autowired
    private UserEnterpriseMapper userEnterpriseMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> registerPersonal(UserPersonal userPersonal) {
        Map<String, Object> result = new HashMap<>();

        // 检查用户名是否已存在
        UserPersonal existingUser = userPersonalMapper.findByUsername(userPersonal.getUsername());
        if (existingUser != null) {
            result.put("success", false);
            result.put("message", "Username already exists");
            return result;
        }

        // 检查邮箱是否已存在
        if (userPersonal.getEmail() != null && !userPersonal.getEmail().isEmpty()) {
            UserPersonal existingEmail = userPersonalMapper.findByEmail(userPersonal.getEmail());
            if (existingEmail != null) {
                result.put("success", false);
                result.put("message", "Email already exists");
                return result;
            }
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(userPersonal.getPassword());
        userPersonal.setPassword(encodedPassword);

        // 插入用户
        int inserted = userPersonalMapper.insert(userPersonal);
        if (inserted > 0) {
            result.put("success", true);
            result.put("message", "Registration successful");
            result.put("userId", userPersonal.getId());
        } else {
            result.put("success", false);
            result.put("message", "Registration failed");
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> registerEnterprise(UserEnterprise userEnterprise) {
        Map<String, Object> result = new HashMap<>();

        // 检查用户名是否已存在
        UserEnterprise existingUser = userEnterpriseMapper.findByUsername(userEnterprise.getUsername());
        if (existingUser != null) {
            result.put("success", false);
            result.put("message", "Username already exists");
            return result;
        }

        // 检查邮箱是否已存在
        if (userEnterprise.getEmail() != null && !userEnterprise.getEmail().isEmpty()) {
            UserEnterprise existingEmail = userEnterpriseMapper.findByEmail(userEnterprise.getEmail());
            if (existingEmail != null) {
                result.put("success", false);
                result.put("message", "Email already exists");
                return result;
            }
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(userEnterprise.getPassword());
        userEnterprise.setPassword(encodedPassword);

        // 设置审核状态为待审核
        userEnterprise.setAuthStatus(0);

        // 插入用户
        int inserted = userEnterpriseMapper.insert(userEnterprise);
        if (inserted > 0) {
            result.put("success", true);
            result.put("message", "Registration successful, pending review");
            result.put("userId", userEnterprise.getId());
        } else {
            result.put("success", false);
            result.put("message", "Registration failed");
        }

        return result;
    }

    @Override
    public Map<String, Object> loginPersonal(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        // 查询用户
        UserPersonal user = userPersonalMapper.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "Username does not exist");
            return result;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "Incorrect password");
            return result;
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), 1);
        result.put("success", true);
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("userType", 1);
        result.put("message", "Login successful");
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());

        return result;
    }

    @Override
    public Map<String, Object> loginEnterprise(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        // 查询用户
        UserEnterprise user = userEnterpriseMapper.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "Username does not exist");
            return result;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "Incorrect password");
            return result;
        }

        // 检查审核状态
        if (user.getAuthStatus() == 0) {
            result.put("success", false);
            result.put("message", "Account is pending review");
            return result;
        } else if (user.getAuthStatus() == 2) {
            result.put("success", false);
            result.put("message", "Account review rejected");
            return result;
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), 2);
        result.put("success", true);
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("userType", 2);
        result.put("message", "Login successful");
        result.put("companyName", user.getCompanyName());
        result.put("contactPerson", user.getContactPerson());
        result.put("email", user.getEmail());
        result.put("logo", user.getLogo());

        return result;
    }

    @Override
    public boolean forgetPassword(String username, String email, Integer userType) {
        // 验证用户信息
        if (userType == 1) {
            UserPersonal user = userPersonalMapper.findByUsername(username);
            if (user == null || !email.equals(user.getEmail())) {
                return false;
            }
        } else if (userType == 2) {
            UserEnterprise user = userEnterpriseMapper.findByUsername(username);
            if (user == null || !email.equals(user.getEmail())) {
                return false;
            }
        } else {
            return false;
        }

        // TODO: 发送重置密码邮件（需要邮件服务）
        // String resetToken = UUID.randomUUID().toString();
        // 存储token到Redis或数据库，设置过期时间
        // 发送邮件包含重置链接

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(String username, String newPassword, String token) {
        // TODO: 验证token的有效性（从Redis或数据库中获取）
        // 这里简化实现，实际需要验证token

        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 更新密码（需要知道用户类型，这里假设通过token可以确定）
        // 实际应该在token中包含用户类型信息
        UserPersonal personalUser = userPersonalMapper.findByUsername(username);
        if (personalUser != null) {
            personalUser.setPassword(encodedPassword);
            return userPersonalMapper.updateById(personalUser) > 0;
        }

        UserEnterprise enterpriseUser = userEnterpriseMapper.findByUsername(username);
        if (enterpriseUser != null) {
            enterpriseUser.setPassword(encodedPassword);
            return userEnterpriseMapper.updateById(enterpriseUser) > 0;
        }

        return false;
    }
}
