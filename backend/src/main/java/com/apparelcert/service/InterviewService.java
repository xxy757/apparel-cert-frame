package com.apparelcert.service;

import com.apparelcert.entity.Interview;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 面试服务接口
 */
public interface InterviewService extends IService<Interview> {
    /**
     * 创建面试邀请
     */
    boolean createInterview(Interview interview);
    
    /**
     * 获取企业面试列表
     */
    Page<Interview> getEnterpriseInterviews(Integer page, Integer size, Long enterpriseId, Integer status);
    
    /**
     * 获取用户面试列表
     */
    Page<Interview> getUserInterviews(Integer page, Integer size, Long userId, Integer status);
    
    /**
     * 更新面试状态
     */
    boolean updateInterviewStatus(Long interviewId, Integer status);
    
    /**
     * 更新面试结果
     */
    boolean updateInterviewResult(Long interviewId, Integer result, String feedback);
}