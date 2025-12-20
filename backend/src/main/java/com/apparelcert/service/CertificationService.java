package com.apparelcert.service;

import com.apparelcert.entity.Certification;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 技能认证服务接口
 */
public interface CertificationService extends IService<Certification> {
    /**
     * 申请技能认证
     */
    boolean applyCertification(Certification certification);
    
    /**
     * 获取用户认证列表
     */
    List<Certification> getByUserId(Long userId);
    
    /**
     * 获取认证详情
     */
    Certification getById(Long id);
    
    /**
     * 上传实操成果
     */
    boolean uploadPractical(Long certificationId, String fileUrl);
    
    /**
     * 获取认证进度
     */
    Integer getProgress(Long certificationId);
    
    /**
     * 分页查询认证记录
     */
    Page<Certification> pageQuery(Integer page, Integer size, Long userId);
}