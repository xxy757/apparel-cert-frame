package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Interview;
import com.apparelcert.service.InterviewService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 面试控制器
 */
@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    /**
     * 创建面试邀请
     */
    @PostMapping
    public Result<Boolean> createInterview(@RequestBody Interview interview) {
        boolean result = interviewService.createInterview(interview);
        return result ? Result.success(true) : Result.error(500, "创建面试邀请失败");
    }

    /**
     * 获取企业面试列表
     */
    @GetMapping("/enterprise")
    public Result<Page<Interview>> getEnterpriseInterviews(
            @RequestParam Long enterpriseId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<Interview> pageInfo = interviewService.getEnterpriseInterviews(page, size, enterpriseId, status);
        return Result.success(pageInfo);
    }

    /**
     * 获取用户面试列表
     */
    @GetMapping("/user")
    public Result<Page<Interview>> getUserInterviews(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<Interview> pageInfo = interviewService.getUserInterviews(page, size, userId, status);
        return Result.success(pageInfo);
    }

    /**
     * 获取面试详情
     */
    @GetMapping("/detail")
    public Result<Map<String, Object>> getInterviewDetail(@RequestParam Long interviewId) {
        Map<String, Object> detail = interviewService.getInterviewDetail(interviewId);
        Boolean success = (Boolean) detail.get("success");
        if (success != null && success) {
            return Result.success(detail);
        } else {
            return Result.error(404, (String) detail.get("message"));
        }
    }

    /**
     * 更新面试状态
     */
    @PutMapping("/status")
    public Result<Boolean> updateInterviewStatus(
            @RequestParam Long interviewId,
            @RequestParam Integer status) {
        boolean result = interviewService.updateInterviewStatus(interviewId, status);
        return result ? Result.success(true) : Result.error(500, "更新状态失败");
    }

    /**
     * 更新面试结果
     */
    @PutMapping("/result")
    public Result<Boolean> updateInterviewResult(@RequestBody Map<String, Object> params) {
        Long interviewId = Long.parseLong(params.get("interviewId").toString());
        Integer result = params.get("result") != null ? Integer.parseInt(params.get("result").toString()) : null;
        String feedback = (String) params.get("feedback");
        
        boolean success = interviewService.updateInterviewResult(interviewId, result, feedback);
        return success ? Result.success(true) : Result.error(500, "更新结果失败");
    }

    /**
     * 记录面试反馈
     */
    @PostMapping("/feedback")
    public Result<Boolean> recordInterviewFeedback(@RequestBody Map<String, Object> params) {
        Long interviewId = Long.parseLong(params.get("interviewId").toString());
        String feedback = (String) params.get("feedback");
        Integer evaluation = params.get("evaluation") != null ? Integer.parseInt(params.get("evaluation").toString()) : null;
        String strengths = (String) params.get("strengths");
        String weaknesses = (String) params.get("weaknesses");
        
        boolean result = interviewService.recordInterviewFeedback(interviewId, feedback, evaluation, strengths, weaknesses);
        return result ? Result.success(true) : Result.error(500, "记录反馈失败");
    }

    /**
     * 更新录用状态
     */
    @PutMapping("/hire-status")
    public Result<Boolean> updateHireStatus(@RequestBody Map<String, Object> params) {
        Long interviewId = Long.parseLong(params.get("interviewId").toString());
        Integer hireStatus = Integer.parseInt(params.get("hireStatus").toString());
        String salary = (String) params.get("salary");
        String entryDate = (String) params.get("entryDate");
        
        boolean result = interviewService.updateHireStatus(interviewId, hireStatus, salary, entryDate);
        return result ? Result.success(true) : Result.error(500, "更新录用状态失败");
    }

    /**
     * 获取面试统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getInterviewStatistics(@RequestParam Long enterpriseId) {
        Map<String, Object> stats = interviewService.getInterviewStatistics(enterpriseId);
        return Result.success(stats);
    }
}
