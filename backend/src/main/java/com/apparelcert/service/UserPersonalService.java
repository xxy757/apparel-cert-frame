package com.apparelcert.service;

import com.apparelcert.entity.UserPersonal;

/**
 * 个人用户服务接口
 */
public interface UserPersonalService {

    /**
     * 更新个人用户基本信息
     * @param userPersonal 用户信息
     * @return 是否更新成功
     */
    boolean updateBasicInfo(UserPersonal userPersonal);

    /**
     * 根据ID获取个人用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    UserPersonal getUserById(Long id);
}