package com.apparelcert.service.impl;

import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.mapper.UserEnterpriseMapper;
import com.apparelcert.service.UserEnterpriseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 企业用户服务实现类
 */
@Service
public class UserEnterpriseServiceImpl extends ServiceImpl<UserEnterpriseMapper, UserEnterprise> implements UserEnterpriseService {

    @Autowired
    private UserEnterpriseMapper userEnterpriseMapper;

    @Override
    public UserEnterprise getById(Long id) {
        return userEnterpriseMapper.selectById(id);
    }

    @Override
    public boolean updateEnterpriseInfo(UserEnterprise userEnterprise) {
        return this.updateById(userEnterprise);
    }

    @Override
    public boolean submitEnterpriseAuth(Long enterpriseId, String businessLicense, String otherCertificates) {
        UserEnterprise enterprise = userEnterpriseMapper.selectById(enterpriseId);
        if (enterprise != null) {
            enterprise.setBusinessLicense(businessLicense);
            // TODO: 保存其他认证文件
            enterprise.setAuthStatus(1); // 1: 待审核
            return this.updateById(enterprise);
        }
        return false;
    }

    @Override
    public Integer getAuthStatus(Long enterpriseId) {
        UserEnterprise enterprise = userEnterpriseMapper.selectById(enterpriseId);
        return enterprise != null ? enterprise.getAuthStatus() : 0;
    }

    @Override
    public boolean updateAuthStatus(Long enterpriseId, Integer authStatus) {
        UserEnterprise enterprise = new UserEnterprise();
        enterprise.setId(enterpriseId);
        enterprise.setAuthStatus(authStatus);
        return this.updateById(enterprise);
    }
}