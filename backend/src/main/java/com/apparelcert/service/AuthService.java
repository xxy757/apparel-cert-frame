package com.apparelcert.service;

import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.entity.UserPersonal;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 个人用户注册
     */
    Map<String, Object> registerPersonal(UserPersonal userPersonal);

    /**
     * 企业用户注册
     */
    Map<String, Object> registerEnterprise(UserEnterprise userEnterprise);

    /**
     * 个人用户登录
     */
    Map<String, Object> loginPersonal(String username, String password);

    /**
     * 企业用户登录
     */
    Map<String, Object> loginEnterprise(String username, String password);

    /**
     * 忘记密码
     */
    boolean forgetPassword(String username, String email, Integer userType);

    /**
     * 重置密码
     */
    boolean resetPassword(String username, String newPassword, String token);
}
