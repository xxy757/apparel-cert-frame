package com.apparelcert.service.impl;

import com.apparelcert.entity.Interview;
import com.apparelcert.mapper.InterviewMapper;
import com.apparelcert.service.InterviewService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 面试服务实现类
 */
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview> implements InterviewService {

    @Autowired
    private InterviewMapper interviewMapper;

    @Override
    public boolean createInterview(Interview interview) {
        interview.setStatus(0); // 0: 待确认
        return this.save(interview);
    }

    @Override
    public Page<Interview> getEnterpriseInterviews(Integer page, Integer size, Long enterpriseId, Integer status) {
        Page<Interview> pageInfo = new Page<>(page, size);
        QueryWrapper<Interview> wrapper = new QueryWrapper<>();
        wrapper.eq("enterprise_id", enterpriseId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("interview_time");
        return interviewMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Page<Interview> getUserInterviews(Integer page, Integer size, Long userId, Integer status) {
        Page<Interview> pageInfo = new Page<>(page, size);
        QueryWrapper<Interview> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("interview_time");
        return interviewMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public boolean updateInterviewStatus(Long interviewId, Integer status) {
        Interview interview = new Interview();
        interview.setId(interviewId);
        interview.setStatus(status);
        return this.updateById(interview);
    }

    @Override
    public boolean updateInterviewResult(Long interviewId, Integer result, String feedback) {
        Interview interview = new Interview();
        interview.setId(interviewId);
        interview.setResult(result);
        interview.setFeedback(feedback);
        return this.updateById(interview);
    }
    
    @Override
    public boolean recordInterviewFeedback(Long interviewId, String feedback, Integer evaluation, String strengths, String weaknesses) {
        Interview interview = this.getById(interviewId);
        if (interview == null) {
            return false;
        }
        
        interview.setFeedback(feedback);
        interview.setEvaluation(evaluation);
        interview.setStrengths(strengths);
        interview.setWeaknesses(weaknesses);
        interview.setStatus(2); // 已面试
        
        return this.updateById(interview);
    }
    
    @Override
    public boolean updateHireStatus(Long interviewId, Integer hireStatus, String salary, String entryDate) {
        Interview interview = this.getById(interviewId);
        if (interview == null) {
            return false;
        }
        
        interview.setHireStatus(hireStatus);
        if (hireStatus == 1) { // 录用
            interview.setSalary(salary);
            interview.setEntryDate(entryDate);
            interview.setResult(1); // 通过
        } else if (hireStatus == 2) { // 不录用
            interview.setResult(0); // 未通过
        }
        
        return this.updateById(interview);
    }
    
    @Override
    public Map<String, Object> getInterviewDetail(Long interviewId) {
        Map<String, Object> result = new HashMap<>();
        
        Interview interview = this.getById(interviewId);
        if (interview == null) {
            result.put("success", false);
            result.put("message", "面试记录不存在");
            return result;
        }
        
        result.put("success", true);
        result.put("interview", interview);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getInterviewStatistics(Long enterpriseId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总面试数
        QueryWrapper<Interview> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("enterprise_id", enterpriseId);
        long totalCount = this.count(totalWrapper);
        stats.put("totalCount", totalCount);
        
        // 待面试数
        QueryWrapper<Interview> pendingWrapper = new QueryWrapper<>();
        pendingWrapper.eq("enterprise_id", enterpriseId);
        pendingWrapper.in("status", 0, 1);
        long pendingCount = this.count(pendingWrapper);
        stats.put("pendingCount", pendingCount);
        
        // 已面试数
        QueryWrapper<Interview> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("enterprise_id", enterpriseId);
        completedWrapper.eq("status", 2);
        long completedCount = this.count(completedWrapper);
        stats.put("completedCount", completedCount);
        
        // 录用数
        QueryWrapper<Interview> hiredWrapper = new QueryWrapper<>();
        hiredWrapper.eq("enterprise_id", enterpriseId);
        hiredWrapper.eq("hire_status", 1);
        long hiredCount = this.count(hiredWrapper);
        stats.put("hiredCount", hiredCount);
        
        // 本月面试数
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date monthStart = cal.getTime();
        
        QueryWrapper<Interview> monthWrapper = new QueryWrapper<>();
        monthWrapper.eq("enterprise_id", enterpriseId);
        monthWrapper.ge("interview_time", monthStart);
        long monthCount = this.count(monthWrapper);
        stats.put("monthCount", monthCount);
        
        // 录用率
        double hireRate = completedCount > 0 ? (double) hiredCount / completedCount * 100 : 0;
        stats.put("hireRate", String.format("%.1f", hireRate));
        
        return stats;
    }
}