package com.apparelcert.service;

import com.apparelcert.entity.Resume;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 简历服务接口
 */
public interface ResumeService extends IService<Resume> {
    /**
     * 获取用户简历
     */
    Resume getByUserId(Long userId);

    /**
     * 获取用户简历数据（包含结构化数据）
     */
    Map<String, Object> getResumeByUserId(Long userId);
    
    /**
     * 保存简历数据
     */
    boolean saveResumeData(Long userId, Map<String, Object> resumeData);
    
    /**
     * 保存或更新简历
     */
    boolean saveOrUpdateResume(Resume resume);
    
    /**
     * 导出简历为PDF
     */
    void exportToPdf(Long userId, HttpServletResponse response);
    
    /**
     * 设置简历公开状态
     */
    boolean setPublicStatus(Long resumeId, Integer isPublic);

    /**
     * 根据用户ID设置简历公开状态
     */
    boolean setPublicStatusByUserId(Long userId, Integer isPublic);
}
