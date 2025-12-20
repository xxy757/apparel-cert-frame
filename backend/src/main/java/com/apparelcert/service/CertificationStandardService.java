package com.apparelcert.service;

import com.apparelcert.entity.CertificationStandard;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 认证标准服务接口
 */
public interface CertificationStandardService extends IService<CertificationStandard> {
    /**
     * 分页查询认证标准列表
     */
    Page<CertificationStandard> pageQuery(Integer page, Integer size, String keyword, String type, Integer level);
    
    /**
     * 获取认证标准详情
     */
    CertificationStandard getById(Long id);
    
    /**
     * 保存或更新认证标准
     */
    boolean saveOrUpdateStandard(CertificationStandard standard);
    
    /**
     * 删除认证标准
     */
    boolean deleteStandard(Long id);
    
    /**
     * 获取所有认证标准类型
     */
    List<String> getAllTypes();
    
    /**
     * 根据类型获取认证标准
     */
    List<CertificationStandard> getByType(String type);
    
    /**
     * 根据类型和等级获取认证标准
     */
    CertificationStandard getByTypeAndLevel(String type, Integer level);
}