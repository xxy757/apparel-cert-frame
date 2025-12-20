package com.apparelcert.service;

import com.apparelcert.entity.Application;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 简历投递服务接口
 */
public interface ApplicationService extends IService<Application> {
    /**
     * 投递简历
     */
    boolean applyJob(Long jobId, Long userId, String resumeUrl);
    
    /**
     * 获取用户投递记录
     */
    List<Application> getByUserId(Long userId);
    
    /**
     * 获取岗位投递记录
     */
    List<Application> getByJobId(Long jobId);
    
    /**
     * 分页查询投递记录
     */
    Page<Application> pageQuery(Integer page, Integer size, Long userId, Long jobId, Integer status);
    
    /**
     * 更新投递状态
     */
    boolean updateStatus(Long applicationId, Integer status);
}