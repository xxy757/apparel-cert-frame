package com.apparelcert.service;

import com.apparelcert.entity.JobPosition;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 职位服务接口
 */
public interface JobPositionService extends IService<JobPosition> {

    /**
     * 发布职位
     */
    boolean publishJob(JobPosition jobPosition);

    /**
     * 更新职位
     */
    boolean updateJob(JobPosition jobPosition);

    /**
     * 删除职位
     */
    boolean deleteJob(Long id);

    /**
     * 获取企业发布的职位列表
     */
    List<JobPosition> getEnterpriseJobs(Long enterpriseId, Map<String, Object> params);

    /**
     * 获取职位详情
     */
    JobPosition getJobDetail(Long id);

    /**
     * 结束招聘
     */
    boolean stopRecruitment(Long id);

    /**
     * 重新招聘
     */
    boolean resumeRecruitment(Long id);

    /**
     * 搜索职位
     */
    List<JobPosition> searchJobs(Map<String, Object> params);
}
