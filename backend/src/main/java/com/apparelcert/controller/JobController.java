package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Application;
import com.apparelcert.entity.Job;
import com.apparelcert.service.ApplicationService;
import com.apparelcert.service.JobService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 求职服务控制器
 */
@RestController
@RequestMapping("/api/personal/job")
public class JobController {

    @Autowired
    private JobService jobService;
    
    @Autowired
    private ApplicationService applicationService;

    /**
     * 分页查询岗位列表
     */
    @GetMapping
    public Result<Page<Job>> getJobList(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String salary) {
        Page<Job> pageInfo = jobService.pageQuery(page, size, keyword, type, location, salary);
        return Result.success(pageInfo);
    }

    /**
     * 获取岗位详情
     */
    @GetMapping("/detail")
    public Result<Job> getJobDetail(@RequestParam Long jobId) {
        // 增加岗位浏览量
        jobService.increaseViews(jobId);
        Job job = jobService.getById(jobId);
        return Result.success(job);
    }

    /**
     * 获取推荐岗位
     */
    @GetMapping("/recommended")
    public Result<List<Job>> getRecommendedJobs(@RequestParam Long userId) {
        List<Job> jobs = jobService.getRecommendedJobs(userId);
        return Result.success(jobs);
    }

    /**
     * 搜索岗位
     */
    @GetMapping("/search")
    public Result<List<Job>> searchJobs(@RequestParam String keyword) {
        List<Job> jobs = jobService.searchJobs(keyword);
        return Result.success(jobs);
    }

    /**
     * 投递简历
     */
    @PostMapping("/apply")
    public Result<Boolean> applyJob(@RequestParam Long jobId, @RequestParam Long userId, @RequestParam String resumeUrl) {
        boolean result = applicationService.applyJob(jobId, userId, resumeUrl);
        return Result.success(result);
    }

    /**
     * 获取投递记录
     */
    @GetMapping("/applications")
    public Result<List<Application>> getApplications(@RequestParam Long userId) {
        List<Application> applications = applicationService.getByUserId(userId);
        return Result.success(applications);
    }
}