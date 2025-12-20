package com.apparelcert.service;

import com.apparelcert.entity.Job;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 岗位服务接口
 */
public interface JobService extends IService<Job> {
    /**
     * 分页查询岗位列表
     */
    Page<Job> pageQuery(Integer page, Integer size, String keyword, String type, String location, String salary);
    
    /**
     * 获取岗位详情
     */
    Job getById(Long id);
    
    /**
     * 增加岗位浏览量
     */
    boolean increaseViews(Long jobId);
    
    /**
     * 获取推荐岗位
     */
    List<Job> getRecommendedJobs(Long userId);
    
    /**
     * 搜索岗位
     */
    List<Job> searchJobs(String keyword);
    
    /**
     * 按企业ID和状态分页查询岗位列表
     */
    Page<Job> getEnterpriseJobs(Integer page, Integer size, Long enterpriseId, Integer status);
}