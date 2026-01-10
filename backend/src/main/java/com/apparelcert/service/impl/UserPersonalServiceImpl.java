package com.apparelcert.service.impl;

import com.apparelcert.entity.UserPersonal;
import com.apparelcert.mapper.UserPersonalMapper;
import com.apparelcert.service.UserPersonalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 个人用户服务实现类
 */
@Service
public class UserPersonalServiceImpl implements UserPersonalService {

    @Autowired
    private UserPersonalMapper userPersonalMapper;

    @Override
    public boolean updateBasicInfo(UserPersonal userPersonal) {
        // 只更新基本信息，不更新密码、状态等敏感字段
        int result = userPersonalMapper.updateById(userPersonal);
        return result > 0;
    }

    @Override
    public UserPersonal getUserById(Long id) {
        return userPersonalMapper.selectById(id);
    }
}