package com.apparelcert.service.impl;

import com.apparelcert.entity.JobPosition;
import com.apparelcert.mapper.JobPositionMapper;
import com.apparelcert.service.JobPositionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 职位服务实现类
 */
@Service
public class JobPositionServiceImpl extends ServiceImpl<JobPositionMapper, JobPosition> implements JobPositionService {

    @Override
    public boolean publishJob(JobPosition jobPosition) {
        // 设置默认值
        jobPosition.setViewCount(0);
        jobPosition.setApplyCount(0);
        jobPosition.setStatus(1); // 1表示招聘中
        return save(jobPosition);
    }

    @Override
    public boolean updateJob(JobPosition jobPosition) {
        return updateById(jobPosition);
    }

    @Override
    public boolean deleteJob(Long id) {
        JobPosition jobPosition = getById(id);
        if (jobPosition != null) {
            jobPosition.setStatus(0); // 0表示已结束
            return updateById(jobPosition);
        }
        return false;
    }

    @Override
    public List<JobPosition> getEnterpriseJobs(Long enterpriseId, Map<String, Object> params) {
        // TODO: 根据企业ID和参数查询职位列表
        return list();
    }

    @Override
    public JobPosition getJobDetail(Long id) {
        return getById(id);
    }

    @Override
    public boolean stopRecruitment(Long id) {
        JobPosition jobPosition = getById(id);
        if (jobPosition != null) {
            jobPosition.setStatus(0); // 0表示已结束
            return updateById(jobPosition);
        }
        return false;
    }

    @Override
    public boolean resumeRecruitment(Long id) {
        JobPosition jobPosition = getById(id);
        if (jobPosition != null) {
            jobPosition.setStatus(1); // 1表示招聘中
            return updateById(jobPosition);
        }
        return false;
    }

    @Override
    public List<JobPosition> searchJobs(Map<String, Object> params) {
        // TODO: 根据参数搜索职位
        return list();
    }
}
