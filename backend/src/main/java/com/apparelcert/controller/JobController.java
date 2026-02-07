package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Application;
import com.apparelcert.entity.Job;
import com.apparelcert.service.ApplicationService;
import com.apparelcert.service.JobService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Result<List<Map<String, Object>>> getRecommendedJobs(@RequestParam Long userId) {
        Map<String, Object> smartResult = jobService.getSmartRecommendedJobs(userId, 1, 10);
        Object recordsObj = smartResult.get("records");

        List<Map<String, Object>> recommendedList = new ArrayList<>();
        if (recordsObj instanceof List) {
            List<?> records = (List<?>) recordsObj;
            for (Object recordObj : records) {
                if (!(recordObj instanceof Map)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> record = (Map<String, Object>) recordObj;
                Object jobObj = record.get("job");
                if (!(jobObj instanceof Job)) {
                    continue;
                }

                Job job = (Job) jobObj;
                Map<String, Object> item = new HashMap<>();
                item.put("id", job.getId());
                item.put("title", job.getTitle());
                item.put("type", job.getType());
                item.put("companyName", job.getCompanyName());
                item.put("companyLogo", job.getCompanyLogo());
                item.put("salary", job.getSalary());
                item.put("location", job.getLocation());
                item.put("description", job.getDescription());
                item.put("requirements", job.getRequirements());
                item.put("benefits", job.getBenefits());
                item.put("education", job.getEducation());
                item.put("experience", job.getExperience());
                item.put("matchScore", record.get("matchScore"));
                item.put("matchReasons", record.get("matchReasons"));
                recommendedList.add(item);
            }
        }

        return Result.success(recommendedList);
    }
    
    /**
     * 获取智能推荐岗位（带匹配度）
     */
    @GetMapping("/smart-recommended")
    public Result<java.util.Map<String, Object>> getSmartRecommendedJobs(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        java.util.Map<String, Object> result = jobService.getSmartRecommendedJobs(userId, page, size);
        return Result.success(result);
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
     * 批量投递简历
     */
    @PostMapping("/batch-apply")
    public Result<java.util.Map<String, Object>> batchApplyJobs(@RequestBody java.util.Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        java.util.List<Long> jobIds = ((java.util.List<Number>) params.get("jobIds"))
            .stream().map(Number::longValue).collect(java.util.stream.Collectors.toList());
        Long userId = Long.parseLong(params.get("userId").toString());
        String resumeUrl = (String) params.get("resumeUrl");
        
        java.util.Map<String, Object> result = applicationService.batchApplyJobs(jobIds, userId, resumeUrl);
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
