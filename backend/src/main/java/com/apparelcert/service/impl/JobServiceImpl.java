package com.apparelcert.service.impl;

import com.apparelcert.entity.Job;
import com.apparelcert.mapper.JobMapper;
import com.apparelcert.service.JobService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位服务实现类
 */
@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {

    @Autowired
    private JobMapper jobMapper;

    @Override
    public Page<Job> pageQuery(Integer page, Integer size, String keyword, String type, String location, String salary) {
        Page<Job> pageInfo = new Page<>(page, size);
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 只显示招聘中的岗位
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("title", keyword).or().like("description", keyword);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (location != null && !location.isEmpty()) {
            wrapper.eq("location", location);
        }
        if (salary != null && !salary.isEmpty()) {
            wrapper.eq("salary", salary);
        }
        
        wrapper.orderByDesc("create_time");
        return jobMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Job getById(Long id) {
        return jobMapper.selectById(id);
    }

    @Override
    public boolean increaseViews(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job != null) {
            job.setViews(job.getViews() + 1);
            return this.updateById(job);
        }
        return false;
    }

    @Override
    public List<Job> getRecommendedJobs(Long userId) {
        // TODO: 实现基于用户技能和偏好的岗位推荐算法
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("views");
        wrapper.last("limit 10");
        return jobMapper.selectList(wrapper);
    }

    @Override
    public List<Job> searchJobs(String keyword) {
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.like("title", keyword).or().like("description", keyword);
        wrapper.orderByDesc("create_time");
        wrapper.last("limit 20");
        return jobMapper.selectList(wrapper);
    }

    @Override
    public Page<Job> getEnterpriseJobs(Integer page, Integer size, Long enterpriseId, Integer status) {
        Page<Job> pageInfo = new Page<>(page, size);
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("enterprise_id", enterpriseId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return jobMapper.selectPage(pageInfo, wrapper);
    }
}