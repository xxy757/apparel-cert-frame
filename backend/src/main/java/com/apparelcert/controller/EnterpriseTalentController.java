package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Application;
import com.apparelcert.entity.Interview;
import com.apparelcert.entity.Resume;
import com.apparelcert.service.ApplicationService;
import com.apparelcert.service.InterviewService;
import com.apparelcert.service.ResumeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业用户人才筛选与面试管理控制器
 */
@RestController
@RequestMapping("/api/enterprise/talent")
public class EnterpriseTalentController {

    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private InterviewService interviewService;
    
    @Autowired
    private ResumeService resumeService;

    /**
     * 获取岗位投递记录
     */
    @GetMapping("/applications")
    public Result<Page<Application>> getApplications(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam Long jobId,
            @RequestParam(required = false) Integer status) {
        Page<Application> pageInfo = applicationService.pageQuery(page, size, null, jobId, status);
        return Result.success(pageInfo);
    }

    /**
     * 更新投递状态
     */
    @PutMapping("/application/status")
    public Result<Boolean> updateApplicationStatus(
            @RequestParam Long applicationId,
            @RequestParam Integer status) {
        boolean result = applicationService.updateStatus(applicationId, status);
        return Result.success(result);
    }

    /**
     * 获取简历详情
     */
    @GetMapping("/resume")
    public Result<Resume> getResumeDetail(@RequestParam Long resumeId) {
        Resume resume = resumeService.getById(resumeId);
        return Result.success(resume);
    }

    /**
     * 发送面试邀请
     */
    @PostMapping("/interview")
    public Result<Boolean> sendInterviewInvitation(@RequestBody Interview interview) {
        boolean result = interviewService.createInterview(interview);
        return Result.success(result);
    }

    /**
     * 获取企业面试列表
     */
    @GetMapping("/interviews")
    public Result<Page<Interview>> getEnterpriseInterviews(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam Long enterpriseId,
            @RequestParam(required = false) Integer status) {
        Page<Interview> pageInfo = interviewService.getEnterpriseInterviews(page, size, enterpriseId, status);
        return Result.success(pageInfo);
    }

    /**
     * 更新面试状态
     */
    @PutMapping("/interview/status")
    public Result<Boolean> updateInterviewStatus(
            @RequestParam Long interviewId,
            @RequestParam Integer status) {
        boolean result = interviewService.updateInterviewStatus(interviewId, status);
        return Result.success(result);
    }

    /**
     * 更新面试结果
     */
    @PutMapping("/interview/result")
    public Result<Boolean> updateInterviewResult(
            @RequestParam Long interviewId,
            @RequestParam Integer result,
            @RequestParam(required = false) String feedback) {
        boolean updateResult = interviewService.updateInterviewResult(interviewId, result, feedback);
        return Result.success(updateResult);
    }

    /**
     * 搜索人才
     */
    @GetMapping("/search")
    public Result<List<Resume>> searchTalents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String careerDirection,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String certification) {
        // TODO: 实现基于关键词、职业方向、学历和认证的人才搜索功能
        return Result.success(null);
    }
}