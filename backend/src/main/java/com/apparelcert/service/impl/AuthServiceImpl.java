package com.apparelcert.service.impl;

import com.apparelcert.entity.UserAdmin;
import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.mapper.UserAdminMapper;
import com.apparelcert.mapper.UserEnterpriseMapper;
import com.apparelcert.mapper.UserPersonalMapper;
import com.apparelcert.service.AuthService;
import com.apparelcert.service.AvatarService;
import com.apparelcert.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private UserAdminMapper userAdminMapper;

    @Autowired
    private AvatarService avatarService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> registerPersonal(UserPersonal userPersonal) {
        Map<String, Object> result = new HashMap<>();

        // 检查用户名是否已存在
        UserPersonal existingUser = userPersonalMapper.findByUsername(userPersonal.getUsername());
        if (existingUser != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }

        // 检查邮箱是否已存在
        if (userPersonal.getEmail() != null && !userPersonal.getEmail().isEmpty()) {
            UserPersonal existingEmail = userPersonalMapper.findByEmail(userPersonal.getEmail());
            if (existingEmail != null) {
                result.put("success", false);
                result.put("message", "邮箱已被注册");
                return result;
            }
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(userPersonal.getPassword());
        userPersonal.setPassword(encodedPassword);

        // 自动分配随机头像（如果用户没有设置头像）
        if (userPersonal.getAvatar() == null || userPersonal.getAvatar().trim().isEmpty()) {
            String avatarUrl = avatarService.generateAvatarByUsername(userPersonal.getUsername());
            userPersonal.setAvatar(avatarUrl);
        }

        // 设置用户状态为正常
        userPersonal.setStatus(1);

        // 插入用户
        int inserted = userPersonalMapper.insert(userPersonal);
        if (inserted > 0) {
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("userId", userPersonal.getId());
        } else {
            result.put("success", false);
            result.put("message", "注册失败，请稍后重试");
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
            result.put("message", "用户名已存在");
            return result;
        }

        // 检查邮箱是否已存在
        if (userEnterprise.getEmail() != null && !userEnterprise.getEmail().isEmpty()) {
            UserEnterprise existingEmail = userEnterpriseMapper.findByEmail(userEnterprise.getEmail());
            if (existingEmail != null) {
                result.put("success", false);
                result.put("message", "邮箱已被注册");
                return result;
            }
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(userEnterprise.getPassword());
        userEnterprise.setPassword(encodedPassword);

        // 自动分配随机Logo（如果企业没有设置Logo）
        if (userEnterprise.getLogo() == null || userEnterprise.getLogo().trim().isEmpty()) {
            String logoUrl = avatarService.generateLogoByCompanyName(userEnterprise.getCompanyName());
            userEnterprise.setLogo(logoUrl);
        }

        // 设置审核状态为待审核
        userEnterprise.setAuthStatus(0);

        // 插入用户
        int inserted = userEnterpriseMapper.insert(userEnterprise);
        if (inserted > 0) {
            result.put("success", true);
            result.put("message", "注册成功，请等待管理员审核");
            result.put("userId", userEnterprise.getId());
        } else {
            result.put("success", false);
            result.put("message", "注册失败，请稍后重试");
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
            result.put("message", "用户不存在");
            return result;
        }

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            result.put("success", false);
            result.put("message", "账户已被禁用");
            return result;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), 1);
        result.put("success", true);
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("userType", 1);
        result.put("message", "登录成功");
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
            result.put("message", "用户不存在");
            return result;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        // 检查审核状态
        if (user.getAuthStatus() == 0) {
            result.put("success", false);
            result.put("message", "账户正在审核中，请耐心等待");
            return result;
        } else if (user.getAuthStatus() == 2) {
            result.put("success", false);
            result.put("message", "账户审核未通过，请联系管理员");
            return result;
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), 2);
        result.put("success", true);
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("userType", 2);
        result.put("message", "登录成功");
        result.put("companyName", user.getCompanyName());
        result.put("contactPerson", user.getContactPerson());
        result.put("email", user.getEmail());
        result.put("logo", user.getLogo());

        return result;
    }

    @Override
    public Map<String, Object> loginAdmin(String username, String password) {
        Map<String, Object> result = new HashMap<>();


        // 查询管理员
        UserAdmin user = userAdminMapper.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "管理员不存在");
            return result;
        }

        // 检查管理员状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            result.put("success", false);
            result.put("message", "账户已被禁用");
            return result;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        // 生成token（userType=3表示管理员）
        String token = jwtUtil.generateToken(user.getId(), 3);
        result.put("success", true);
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("userType", 3);
        result.put("message", "登录成功");
        result.put("username", user.getUsername());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("adminType", user.getAdminType());
        result.put("avatar", user.getAvatar());

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

    @Override
    public Map<String, Object> getCurrentUser(String token) {
        Map<String, Object> result = new HashMap<>();

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "登录已过期，请重新登录");
            return result;
        }

        // 从token中获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        Integer userType = jwtUtil.getUserTypeFromToken(token);

        if (userId == null || userType == null) {
            result.put("success", false);
            result.put("message", "认证信息无效");
            return result;
        }

        result.put("success", true);
        result.put("userId", userId);
        result.put("userType", userType);

        if (userType == 1) {
            // 个人用户
            UserPersonal user = userPersonalMapper.selectById(userId);
            if (user != null) {
                result.put("username", user.getUsername());
                result.put("name", user.getName());
                result.put("email", user.getEmail());
                result.put("phone", user.getPhone());
                result.put("avatar", user.getAvatar());
                result.put("careerDirection", user.getCareerDirection());
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        } else if (userType == 2) {
            // 企业用户
            UserEnterprise user = userEnterpriseMapper.selectById(userId);
            if (user != null) {
                result.put("username", user.getUsername());
                result.put("companyName", user.getCompanyName());
                result.put("contactPerson", user.getContactPerson());
                result.put("email", user.getEmail());
                result.put("contactPhone", user.getContactPhone());
                result.put("logo", user.getLogo());
                result.put("authStatus", user.getAuthStatus());
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        } else if (userType == 3) {
            // 管理员用户
            UserAdmin user = userAdminMapper.selectById(userId);
            if (user != null) {
                result.put("username", user.getUsername());
                result.put("name", user.getName());
                result.put("email", user.getEmail());
                result.put("phone", user.getPhone());
                result.put("avatar", user.getAvatar());
                result.put("adminType", user.getAdminType());
                result.put("lastLoginTime", user.getLastLoginTime());
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        }

        return result;
    }
    
    @Override
    public boolean checkEmailExists(String email, Integer userType) {
        if (userType == 1) {
            UserPersonal user = userPersonalMapper.findByEmail(email);
            return user != null;
        } else if (userType == 2) {
            UserEnterprise user = userEnterpriseMapper.findByEmail(email);
            return user != null;
        }
        return false;
    }
    
    @Override
    public boolean checkUsernameExists(String username, Integer userType) {
        if (userType == 1) {
            UserPersonal user = userPersonalMapper.findByUsername(username);
            return user != null;
        } else if (userType == 2) {
            UserEnterprise user = userEnterpriseMapper.findByUsername(username);
            return user != null;
        }
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPasswordByEmail(String email, String newPassword, Integer userType) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        
        if (userType == 1) {
            UserPersonal user = userPersonalMapper.findByEmail(email);
            if (user != null) {
                user.setPassword(encodedPassword);
                return userPersonalMapper.updateById(user) > 0;
            }
        } else if (userType == 2) {
            UserEnterprise user = userEnterpriseMapper.findByEmail(email);
            if (user != null) {
                user.setPassword(encodedPassword);
                return userEnterpriseMapper.updateById(user) > 0;
            }
        }
        return false;
    }
}
