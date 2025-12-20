package com.apparelcert.service;

import com.apparelcert.entity.UserEnterprise;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 企业用户服务接口
 */
public interface UserEnterpriseService extends IService<UserEnterprise> {
    /**
     * 获取企业信息
     */
    UserEnterprise getById(Long id);
    
    /**
     * 更新企业信息
     */
    boolean updateEnterpriseInfo(UserEnterprise userEnterprise);
    
    /**
     * 提交企业认证
     */
    boolean submitEnterpriseAuth(Long enterpriseId, String businessLicense, String otherCertificates);
    
    /**
     * 获取企业认证状态
     */
    Integer getAuthStatus(Long enterpriseId);
    
    /**
     * 更新企业认证状态
     */
    boolean updateAuthStatus(Long enterpriseId, Integer authStatus);
}