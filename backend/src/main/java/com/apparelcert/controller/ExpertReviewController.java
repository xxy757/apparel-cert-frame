package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.ExpertReview;
import com.apparelcert.service.ExpertReviewService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 专家评审控制器
 */
@RestController
@RequestMapping("/api/expert/review")
public class ExpertReviewController {

    @Autowired
    private ExpertReviewService expertReviewService;

    /**
     * 获取待评审列表
     */
    @GetMapping("/pending")
    public Result<Page<ExpertReview>> getPendingReviews(
            @RequestParam Long expertId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer reviewType) {
        Page<ExpertReview> pageInfo = expertReviewService.getPendingReviews(expertId, page, size, reviewType);
        return Result.success(pageInfo);
    }

    /**
     * 获取已评审列表
     */
    @GetMapping("/completed")
    public Result<Page<ExpertReview>> getCompletedReviews(
            @RequestParam Long expertId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<ExpertReview> pageInfo = expertReviewService.getCompletedReviews(expertId, page, size);
        return Result.success(pageInfo);
    }

    /**
     * 获取评审详情
     */
    @GetMapping("/detail")
    public Result<Map<String, Object>> getReviewDetail(@RequestParam Long reviewId) {
        Map<String, Object> detail = expertReviewService.getReviewDetail(reviewId);
        Boolean success = (Boolean) detail.get("success");
        if (success != null && success) {
            return Result.success(detail);
        } else {
            return Result.error(404, (String) detail.get("message"));
        }
    }

    /**
     * 提交评审结果
     */
    @PostMapping("/submit")
    public Result<Boolean> submitReview(@RequestBody Map<String, Object> params) {
        Long reviewId = Long.parseLong(params.get("reviewId").toString());
        Integer score = params.get("score") != null ? Integer.parseInt(params.get("score").toString()) : null;
        String grade = (String) params.get("grade");
        String comment = (String) params.get("comment");
        Boolean passed = params.get("passed") != null ? (Boolean) params.get("passed") : true;
        
        boolean result = expertReviewService.submitReview(reviewId, score, grade, comment, passed);
        return result ? Result.success(true) : Result.error(500, "提交评审失败");
    }

    /**
     * 驳回评审
     */
    @PostMapping("/reject")
    public Result<Boolean> rejectReview(@RequestBody Map<String, String> params) {
        Long reviewId = Long.parseLong(params.get("reviewId"));
        String reason = params.get("reason");
        
        boolean result = expertReviewService.rejectReview(reviewId, reason);
        return result ? Result.success(true) : Result.error(500, "驳回失败");
    }

    /**
     * 分配评审任务
     */
    @PostMapping("/assign")
    public Result<Boolean> assignReview(@RequestBody Map<String, Object> params) {
        Long certificationId = Long.parseLong(params.get("certificationId").toString());
        Long expertId = Long.parseLong(params.get("expertId").toString());
        Integer reviewType = Integer.parseInt(params.get("reviewType").toString());
        
        boolean result = expertReviewService.assignReview(certificationId, expertId, reviewType);
        return result ? Result.success(true) : Result.error(500, "分配失败，可能已存在待评审任务");
    }

    /**
     * 批量分配评审任务
     */
    @PostMapping("/batch-assign")
    public Result<Integer> batchAssignReviews(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> certificationIds = (List<Long>) params.get("certificationIds");
        Long expertId = Long.parseLong(params.get("expertId").toString());
        Integer reviewType = Integer.parseInt(params.get("reviewType").toString());
        
        int count = expertReviewService.batchAssignReviews(certificationIds, expertId, reviewType);
        return Result.success(count);
    }

    /**
     * 获取专家评审统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getExpertStatistics(@RequestParam Long expertId) {
        Map<String, Object> stats = expertReviewService.getExpertStatistics(expertId);
        return Result.success(stats);
    }

    /**
     * 获取认证的所有评审记录
     */
    @GetMapping("/by-certification")
    public Result<List<ExpertReview>> getReviewsByCertification(@RequestParam Long certificationId) {
        List<ExpertReview> reviews = expertReviewService.getReviewsByCertification(certificationId);
        return Result.success(reviews);
    }
}
