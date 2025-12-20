package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Job;
import com.apparelcert.service.JobService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 置顶岗位
     */
    @PutMapping("/top")
    public Result<Boolean> topJob(@RequestParam Long jobId) {
        // TODO: 实现岗位置顶功能
        return Result.success(true);
    }
}