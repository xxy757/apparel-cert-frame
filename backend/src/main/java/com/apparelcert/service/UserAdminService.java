package com.apparelcert.service;

import com.apparelcert.entity.UserAdmin;

/**
 * 管理员用户服务接口
 */
public interface UserAdminService {

    /**
     * 根据用户名获取管理员信息
     * @param username 用户名
     * @return 管理员信息
     */
    UserAdmin getUserByUsername(String username);

    /**
     * 根据ID获取管理员信息
     * @param id 管理员ID
     * @return 管理员信息
     */
    UserAdmin getUserById(Long id);

    /**
     * 更新管理员基本信息
     * @param userAdmin 管理员信息
     * @return 是否更新成功
     */
    boolean updateBasicInfo(UserAdmin userAdmin);

    /**
     * 更新最后登录信息
     * @param id 管理员ID
     * @param lastLoginIp 最后登录IP
     * @return 是否更新成功
     */
    boolean updateLastLoginInfo(Long id, String lastLoginIp);

    /**
     * 修改管理员密码
     * @param id 管理员ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);
}
