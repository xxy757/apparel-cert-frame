package com.apparelcert.service;

/**
 * 头像服务接口
 * 为用户生成随机卡通头像
 */
public interface AvatarService {

    /**
     * 生成随机个人用户头像URL
     * @return 头像URL地址
     */
    String generateRandomAvatar();

    /**
     * 基于用户名生成确定性头像URL（同一用户名总是生成相同头像）
     * @param username 用户名
     * @return 头像URL地址
     */
    String generateAvatarByUsername(String username);

    /**
     * 基于企业名称生成确定性Logo URL（使用卡通风格）
     * @param companyName 企业名称
     * @return Logo URL地址
     */
    String generateLogoByCompanyName(String companyName);
}
