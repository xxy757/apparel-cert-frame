package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.ResumeCollection;
import com.apparelcert.service.ResumeCollectionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简历收藏控制器
 */
@RestController
@RequestMapping("/api/enterprise/resume-collection")
public class ResumeCollectionController {

    @Autowired
    private ResumeCollectionService collectionService;

    /**
     * 收藏简历
     */
    @PostMapping("/collect")
    public Result<Boolean> collectResume(
            @RequestParam Long enterpriseId,
            @RequestParam Long resumeUserId) {
        boolean result = collectionService.collectResume(enterpriseId, resumeUserId);
        return Result.success(result);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/uncollect")
    public Result<Boolean> uncollectResume(
            @RequestParam Long enterpriseId,
            @RequestParam Long resumeUserId) {
        boolean result = collectionService.uncollectResume(enterpriseId, resumeUserId);
        return Result.success(result);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> checkCollected(
            @RequestParam Long enterpriseId,
            @RequestParam Long resumeUserId) {
        boolean collected = collectionService.isCollected(enterpriseId, resumeUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("collected", collected);
        if (collected) {
            ResumeCollection detail = collectionService.getCollectionDetail(enterpriseId, resumeUserId);
            result.put("intentionLevel", detail.getIntentionLevel());
            result.put("notes", detail.getNotes());
            result.put("tags", detail.getTags());
        }
        return Result.success(result);
    }

    /**
     * 更新意向程度
     */
    @PutMapping("/intention")
    public Result<Boolean> updateIntentionLevel(
            @RequestParam Long enterpriseId,
            @RequestParam Long resumeUserId,
            @RequestParam Integer intentionLevel) {
        boolean result = collectionService.updateIntentionLevel(enterpriseId, resumeUserId, intentionLevel);
        return Result.success(result);
    }

    /**
     * 更新备注
     */
    @PutMapping("/notes")
    public Result<Boolean> updateNotes(@RequestBody Map<String, Object> params) {
        Long enterpriseId = Long.parseLong(params.get("enterpriseId").toString());
        Long resumeUserId = Long.parseLong(params.get("resumeUserId").toString());
        String notes = (String) params.get("notes");
        
        boolean result = collectionService.updateNotes(enterpriseId, resumeUserId, notes);
        return Result.success(result);
    }

    /**
     * 更新标签
     */
    @PutMapping("/tags")
    public Result<Boolean> updateTags(@RequestBody Map<String, Object> params) {
        Long enterpriseId = Long.parseLong(params.get("enterpriseId").toString());
        Long resumeUserId = Long.parseLong(params.get("resumeUserId").toString());
        String tags = (String) params.get("tags");
        
        boolean result = collectionService.updateTags(enterpriseId, resumeUserId, tags);
        return Result.success(result);
    }

    /**
     * 获取收藏的简历列表
     */
    @GetMapping("/list")
    public Result<Page<ResumeCollection>> getCollectedResumes(
            @RequestParam Long enterpriseId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer intentionLevel) {
        Page<ResumeCollection> pageInfo = collectionService.getCollectedResumes(enterpriseId, page, size, intentionLevel);
        return Result.success(pageInfo);
    }

    /**
     * 批量收藏
     */
    @PostMapping("/batch-collect")
    public Result<Integer> batchCollect(@RequestBody Map<String, Object> params) {
        Long enterpriseId = Long.parseLong(params.get("enterpriseId").toString());
        @SuppressWarnings("unchecked")
        List<Long> resumeUserIds = ((List<Number>) params.get("resumeUserIds"))
            .stream().map(Number::longValue).collect(java.util.stream.Collectors.toList());
        
        int count = collectionService.batchCollect(enterpriseId, resumeUserIds);
        return Result.success(count);
    }

    /**
     * 批量取消收藏
     */
    @DeleteMapping("/batch-uncollect")
    public Result<Integer> batchUncollect(@RequestBody Map<String, Object> params) {
        Long enterpriseId = Long.parseLong(params.get("enterpriseId").toString());
        @SuppressWarnings("unchecked")
        List<Long> resumeUserIds = ((List<Number>) params.get("resumeUserIds"))
            .stream().map(Number::longValue).collect(java.util.stream.Collectors.toList());
        
        int count = collectionService.batchUncollect(enterpriseId, resumeUserIds);
        return Result.success(count);
    }
}
