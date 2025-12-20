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

import java.util.Date;
import java.util.List;

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
        job.setApplications(job.getApplications() + 1);
        jobMapper.updateById(job);
        
        return this.save(application);
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
}