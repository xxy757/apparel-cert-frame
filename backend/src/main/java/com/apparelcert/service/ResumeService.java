package com.apparelcert.service;

import com.apparelcert.entity.Resume;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 简历服务接口
 */
public interface ResumeService extends IService<Resume> {
    /**
     * 获取用户简历
     */
    Resume getByUserId(Long userId);
    
    /**
     * 保存或更新简历
     */
    boolean saveOrUpdateResume(Resume resume);
    
    /**
     * 导出简历为PDF
     */
    String exportToPdf(Long resumeId);
    
    /**
     * 设置简历公开状态
     */
    boolean setPublicStatus(Long resumeId, Integer isPublic);
}