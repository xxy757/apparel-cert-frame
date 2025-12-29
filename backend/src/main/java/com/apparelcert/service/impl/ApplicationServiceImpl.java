package com.apparelcert.service.impl;

import com.apparelcert.entity.Application;
import com.apparelcert.entity.Job;
import com.apparelcert.mapper.ApplicationMapper;
import com.apparelcert.mapper.JobMapper;
import com.apparelcert.service.ApplicationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 简历投递服务实现类
 */
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    @Autowired
    private ApplicationMapper applicationMapper;
    
    @Autowired
    private JobMapper jobMapper;

    @Override
    public boolean applyJob(Long jobId, Long userId, String resumeUrl) {
        // 检查岗位是否存在且处于招聘中
        Job job = jobMapper.selectById(jobId);
        if (job == null || job.getStatus() != 1) {
            return false;
        }
        
        // 检查是否已经投递过
        QueryWrapper<Application> wrapper = new QueryWrapper<>();
        wrapper.eq("job_id", jobId);
        wrapper.eq("user_id", userId);
        if (applicationMapper.selectOne(wrapper) != null) {
            return false;
        }
        
        // 创建投递记录
        Application application = new Application();
        application.setJobId(jobId);
        application.setUserId(userId);
        application.setEnterpriseId(job.getEnterpriseId());
        application.setResumeUrl(resumeUrl);
        application.setStatus(0); // 0: 待处理
        application.setApplyTime(new Date());
        
        // 更新岗位投递数
        job.setApplications((job.getApplications() != null ? job.getApplications() : 0) + 1);
        jobMapper.updateById(job);
        
        return this.save(application);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchApplyJobs(List<Long> jobIds, Long userId, String resumeUrl) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<Map<String, Object>> failedJobs = new ArrayList<>();
        
        for (Long jobId : jobIds) {
            Job job = jobMapper.selectById(jobId);
            
            // 检查岗位状态
            if (job == null) {
                failCount++;
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("jobId", jobId);
                failInfo.put("reason", "岗位不存在");
                failedJobs.add(failInfo);
                continue;
            }
            
            if (job.getStatus() != 1) {
                failCount++;
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("jobId", jobId);
                failInfo.put("jobTitle", job.getTitle());
                failInfo.put("reason", "岗位已停止招聘");
                failedJobs.add(failInfo);
                continue;
            }
            
            // 检查是否已投递
            QueryWrapper<Application> wrapper = new QueryWrapper<>();
            wrapper.eq("job_id", jobId);
            wrapper.eq("user_id", userId);
            if (applicationMapper.selectOne(wrapper) != null) {
                failCount++;
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("jobId", jobId);
                failInfo.put("jobTitle", job.getTitle());
                failInfo.put("reason", "已投递过该岗位");
                failedJobs.add(failInfo);
                continue;
            }
            
            // 创建投递记录
            Application application = new Application();
            application.setJobId(jobId);
            application.setUserId(userId);
            application.setEnterpriseId(job.getEnterpriseId());
            application.setResumeUrl(resumeUrl);
            application.setStatus(0);
            application.setApplyTime(new Date());
            
            if (this.save(application)) {
                // 更新岗位投递数
                job.setApplications((job.getApplications() != null ? job.getApplications() : 0) + 1);
                jobMapper.updateById(job);
                successCount++;
            } else {
                failCount++;
                Map<String, Object> failInfo = new HashMap<>();
                failInfo.put("jobId", jobId);
                failInfo.put("jobTitle", job.getTitle());
                failInfo.put("reason", "投递失败");
                failedJobs.add(failInfo);
            }
        }
        
        result.put("success", true);
        result.put("totalCount", jobIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failedJobs", failedJobs);
        
        return result;
    }

    @Override
    public List<Application> getByUserId(Long userId) {
        QueryWrapper<Application> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("apply_time");
        return applicationMapper.selectList(wrapper);
    }

    @Override
    public List<Application> getByJobId(Long jobId) {
        QueryWrapper<Application> wrapper = new QueryWrapper<>();
        wrapper.eq("job_id", jobId);
        wrapper.orderByDesc("apply_time");
        return applicationMapper.selectList(wrapper);
    }

    @Override
    public Page<Application> pageQuery(Integer page, Integer size, Long userId, Long jobId, Integer status) {
        Page<Application> pageInfo = new Page<>(page, size);
        QueryWrapper<Application> wrapper = new QueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (jobId != null) {
            wrapper.eq("job_id", jobId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("apply_time");
        return applicationMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public boolean updateStatus(Long applicationId, Integer status) {
        Application application = new Application();
        application.setId(applicationId);
        application.setStatus(status);
        return this.updateById(application);
    }
    
    @Override
    public Page<Application> getEnterpriseApplications(Integer page, Integer size, Long enterpriseId, Integer status) {
        Page<Application> pageInfo = new Page<>(page, size);
        QueryWrapper<Application> wrapper = new QueryWrapper<>();
        wrapper.eq("enterprise_id", enterpriseId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("apply_time");
        return applicationMapper.selectPage(pageInfo, wrapper);
    }
}