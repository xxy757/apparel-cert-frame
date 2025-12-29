package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Job;
import com.apparelcert.service.JobService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 企业用户职位管理控制器
 */
@RestController
@RequestMapping("/api/enterprise/job")
public class EnterpriseJobController {

    @Autowired
    private JobService jobService;

    /**
     * 发布岗位
     */
    @PostMapping
    public Result<Boolean> publishJob(@RequestBody Job job) {
        job.setStatus(1); // 1: 招聘中
        job.setViews(0);
        job.setApplications(0);
        boolean result = jobService.save(job);
        return Result.success(result);
    }

    /**
     * 获取企业发布的岗位列表
     */
    @GetMapping
    public Result<Page<Job>> getEnterpriseJobs(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam Long enterpriseId,
            @RequestParam(required = false) Integer status) {
        Page<Job> pageInfo = jobService.getEnterpriseJobs(page, size, enterpriseId, status);
        return Result.success(pageInfo);
    }

    /**
     * 获取岗位详情
     */
    @GetMapping("/detail")
    public Result<Job> getJobDetail(@RequestParam Long jobId) {
        Job job = jobService.getById(jobId);
        return Result.success(job);
    }

    /**
     * 更新岗位信息
     */
    @PutMapping
    public Result<Boolean> updateJob(@RequestBody Job job) {
        boolean result = jobService.updateById(job);
        return Result.success(result);
    }

    /**
     * 下架岗位
     */
    @PutMapping("/offline")
    public Result<Boolean> offlineJob(@RequestParam Long jobId) {
        Job job = new Job();
        job.setId(jobId);
        job.setStatus(0); // 0: 已结束
        boolean result = jobService.updateById(job);
        return Result.success(result);
    }
    
    /**
     * 批量更新岗位状态
     */
    @PutMapping("/batch-status")
    public Result<Integer> batchUpdateStatus(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> jobIds = ((List<Number>) params.get("jobIds"))
            .stream().map(Number::longValue).collect(java.util.stream.Collectors.toList());
        Integer status = Integer.parseInt(params.get("status").toString());
        
        int count = jobService.batchUpdateStatus(jobIds, status);
        return Result.success(count);
    }
    
    /**
     * 批量删除岗位
     */
    @DeleteMapping("/batch")
    public Result<Integer> batchDeleteJobs(@RequestBody List<Long> jobIds) {
        int count = jobService.batchDeleteJobs(jobIds);
        return Result.success(count);
    }
    
    /**
     * 获取岗位统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getJobStatistics(@RequestParam Long jobId) {
        Map<String, Object> stats = jobService.getJobStatistics(jobId);
        return Result.success(stats);
    }
    
    /**
     * 获取企业岗位统计
     */
    @GetMapping("/enterprise-statistics")
    public Result<Map<String, Object>> getEnterpriseJobStatistics(@RequestParam Long enterpriseId) {
        Map<String, Object> stats = jobService.getEnterpriseJobStatistics(enterpriseId);
        return Result.success(stats);
    }

    /**
     * 置顶岗位
     */
    @PutMapping("/top")
    public Result<Boolean> topJob(@RequestParam Long jobId, @RequestParam(defaultValue = "1") Integer isTop) {
        Job job = jobService.getById(jobId);
        if (job == null) {
            return Result.error(404, "岗位不存在");
        }
        // 使用views字段的高位来标记置顶（简化实现）
        // 实际项目应该添加is_top字段
        if (isTop == 1) {
            job.setViews((job.getViews() != null ? job.getViews() : 0) + 1000000);
        } else {
            job.setViews(Math.max(0, (job.getViews() != null ? job.getViews() : 0) - 1000000));
        }
        boolean result = jobService.updateById(job);
        return Result.success(result);
    }
}