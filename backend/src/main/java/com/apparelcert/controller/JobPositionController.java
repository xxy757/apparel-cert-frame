package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.JobPosition;
import com.apparelcert.service.JobPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 职位控制器
 */
@RestController
@RequestMapping("/api/job")
public class JobPositionController {

    @Autowired
    private JobPositionService jobPositionService;

    /**
     * 发布职位
     */
    @PostMapping("/publish")
    public Result<Boolean> publishJob(@RequestBody JobPosition jobPosition) {
        boolean result = jobPositionService.publishJob(jobPosition);
        return Result.success(result);
    }

    /**
     * 更新职位
     */
    @PutMapping("/update")
    public Result<Boolean> updateJob(@RequestBody JobPosition jobPosition) {
        boolean result = jobPositionService.updateJob(jobPosition);
        return Result.success(result);
    }

    /**
     * 删除职位
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteJob(@PathVariable Long id) {
        boolean result = jobPositionService.deleteJob(id);
        return Result.success(result);
    }

    /**
     * 获取企业职位列表
     */
    @GetMapping("/enterprise/{enterpriseId}")
    public Result<List<JobPosition>> getEnterpriseJobs(@PathVariable Long enterpriseId, @RequestParam Map<String, Object> params) {
        List<JobPosition> jobs = jobPositionService.getEnterpriseJobs(enterpriseId, params);
        return Result.success(jobs);
    }

    /**
     * 获取职位详情
     */
    @GetMapping("/detail/{id}")
    public Result<JobPosition> getJobDetail(@PathVariable Long id) {
        JobPosition jobPosition = jobPositionService.getJobDetail(id);
        return Result.success(jobPosition);
    }

    /**
     * 结束招聘
     */
    @PutMapping("/stop/{id}")
    public Result<Boolean> stopRecruitment(@PathVariable Long id) {
        boolean result = jobPositionService.stopRecruitment(id);
        return Result.success(result);
    }

    /**
     * 重新招聘
     */
    @PutMapping("/resume/{id}")
    public Result<Boolean> resumeRecruitment(@PathVariable Long id) {
        boolean result = jobPositionService.resumeRecruitment(id);
        return Result.success(result);
    }

    /**
     * 搜索职位
     */
    @GetMapping("/search")
    public Result<List<JobPosition>> searchJobs(@RequestParam Map<String, Object> params) {
        List<JobPosition> jobs = jobPositionService.searchJobs(params);
        return Result.success(jobs);
    }
}
