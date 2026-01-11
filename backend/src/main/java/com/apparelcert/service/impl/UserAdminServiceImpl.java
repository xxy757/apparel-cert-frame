package com.apparelcert.service.impl;

import com.apparelcert.entity.UserAdmin;
import com.apparelcert.mapper.UserAdminMapper;
import com.apparelcert.service.UserAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 管理员用户服务实现类
 */
@Service
public class UserAdminServiceImpl implements UserAdminService {

    @Autowired
    private UserAdminMapper userAdminMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserAdmin getUserByUsername(String username) {
        return userAdminMapper.findByUsername(username);
    }

    @Override
    public UserAdmin getUserById(Long id) {
        return userAdminMapper.selectById(id);
    }

    @Override
    public boolean updateBasicInfo(UserAdmin userAdmin) {
        // 只更新基本信息，不更新密码等敏感字段
        int result = userAdminMapper.updateById(userAdmin);
        return result > 0;
    }

    @Override
    public boolean updateLastLoginInfo(Long id, String lastLoginIp) {
        int result = userAdminMapper.updateLastLoginInfo(id, lastLoginIp);
        return result > 0;
    }

    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        UserAdmin admin = userAdminMapper.selectById(id);
        if (admin == null) {
            return false;
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            return false;
        }

        // 加密新密码
        String encryptedPassword = passwordEncoder.encode(newPassword);
        admin.setPassword(encryptedPassword);

        int result = userAdminMapper.updateById(admin);
        return result > 0;
    }
}
