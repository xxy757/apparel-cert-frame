package com.apparelcert.service;

import com.apparelcert.entity.Application;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 简历投递服务接口
 */
public interface ApplicationService extends IService<Application> {
    /**
     * 投递简历
     */
    boolean applyJob(Long jobId, Long userId, String resumeUrl);
    
    /**
     * 批量投递简历
     * @param jobIds 岗位ID列表
     * @param userId 用户ID
     * @param resumeUrl 简历URL
     * @return 投递结果（成功数、失败数、失败原因）
     */
    Map<String, Object> batchApplyJobs(List<Long> jobIds, Long userId, String resumeUrl);
    
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
    
    /**
     * 获取企业收到的投递记录
     */
    Page<Application> getEnterpriseApplications(Integer page, Integer size, Long enterpriseId, Integer status);
}