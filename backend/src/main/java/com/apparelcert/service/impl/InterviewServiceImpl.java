package com.apparelcert.service.impl;

import com.apparelcert.entity.Interview;
import com.apparelcert.mapper.InterviewMapper;
import com.apparelcert.service.InterviewService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}