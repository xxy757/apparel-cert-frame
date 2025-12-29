package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Expert;
import com.apparelcert.service.ExpertService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评审专家管理控制器
 */
@RestController
@RequestMapping("/api/admin/expert")
public class ExpertController {

    @Autowired
    private ExpertService expertService;

    /**
     * 添加专家
     */
    @PostMapping
    public Result<Boolean> addExpert(@RequestBody Expert expert) {
        boolean result = expertService.addExpert(expert);
        return result ? Result.success(true) : Result.error(500, "添加失败");
    }

    /**
     * 更新专家信息
     */
    @PutMapping
    public Result<Boolean> updateExpert(@RequestBody Expert expert) {
        boolean result = expertService.updateExpert(expert);
        return result ? Result.success(true) : Result.error(500, "更新失败");
    }

    /**
     * 删除专家
     */
    @DeleteMapping
    public Result<Boolean> deleteExpert(@RequestParam Long expertId) {
        boolean result = expertService.deleteExpert(expertId);
        return result ? Result.success(true) : Result.error(500, "删除失败");
    }

    /**
     * 获取专家详情
     */
    @GetMapping("/detail")
    public Result<Expert> getExpertDetail(@RequestParam Long expertId) {
        Expert expert = expertService.getExpertDetail(expertId);
        return Result.success(expert);
    }

    /**
     * 分页查询专家列表
     */
    @GetMapping("/list")
    public Result<Page<Expert>> pageQuery(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) Integer status) {
        Page<Expert> pageInfo = expertService.pageQuery(page, size, keyword, specialty, status);
        return Result.success(pageInfo);
    }

    /**
     * 根据专业领域获取专家列表
     */
    @GetMapping("/by-specialty")
    public Result<List<Expert>> getExpertsBySpecialty(@RequestParam String specialty) {
        List<Expert> experts = expertService.getExpertsBySpecialty(specialty);
        return Result.success(experts);
    }

    /**
     * 启用/禁用专家
     */
    @PutMapping("/status")
    public Result<Boolean> updateExpertStatus(
            @RequestParam Long expertId,
            @RequestParam Integer status) {
        boolean result = expertService.updateExpertStatus(expertId, status);
        return result ? Result.success(true) : Result.error(500, "更新状态失败");
    }

    /**
     * 获取可用专家列表（用于分配评审任务）
     */
    @GetMapping("/available")
    public Result<List<Expert>> getAvailableExperts(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) Integer reviewLevel) {
        List<Expert> experts = expertService.getAvailableExperts(specialty, reviewLevel);
        return Result.success(experts);
    }
}
