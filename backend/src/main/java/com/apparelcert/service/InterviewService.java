package com.apparelcert.service;

import com.apparelcert.entity.Interview;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

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
    
    /**
     * 记录面试反馈
     * @param interviewId 面试ID
     * @param feedback 面试反馈
     * @param evaluation 面试评价（1-5分）
     * @param strengths 优势
     * @param weaknesses 不足
     * @return 是否成功
     */
    boolean recordInterviewFeedback(Long interviewId, String feedback, Integer evaluation, String strengths, String weaknesses);
    
    /**
     * 更新录用状态
     * @param interviewId 面试ID
     * @param hireStatus 录用状态：0-待定 1-录用 2-不录用
     * @param salary 薪资（录用时）
     * @param entryDate 入职日期（录用时）
     * @return 是否成功
     */
    boolean updateHireStatus(Long interviewId, Integer hireStatus, String salary, String entryDate);
    
    /**
     * 获取面试详情（包含完整反馈）
     */
    Map<String, Object> getInterviewDetail(Long interviewId);
    
    /**
     * 获取面试统计
     */
    Map<String, Object> getInterviewStatistics(Long enterpriseId);
}