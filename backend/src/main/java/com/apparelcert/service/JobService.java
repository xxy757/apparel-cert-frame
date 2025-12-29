package com.apparelcert.service;

import com.apparelcert.entity.Job;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

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
     * 获取推荐岗位（基于用户技能和偏好）
     */
    List<Job> getRecommendedJobs(Long userId);
    
    /**
     * 获取智能推荐岗位（带匹配度）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 推荐岗位列表（包含匹配度信息）
     */
    Map<String, Object> getSmartRecommendedJobs(Long userId, Integer page, Integer size);
    
    /**
     * 搜索岗位
     */
    List<Job> searchJobs(String keyword);
    
    /**
     * 按企业ID和状态分页查询岗位列表
     */
    Page<Job> getEnterpriseJobs(Integer page, Integer size, Long enterpriseId, Integer status);
    
    /**
     * 批量更新岗位状态
     * @param jobIds 岗位ID列表
     * @param status 状态
     * @return 成功更新数量
     */
    int batchUpdateStatus(List<Long> jobIds, Integer status);
    
    /**
     * 批量删除岗位
     * @param jobIds 岗位ID列表
     * @return 成功删除数量
     */
    int batchDeleteJobs(List<Long> jobIds);
    
    /**
     * 获取岗位统计数据
     * @param jobId 岗位ID
     * @return 统计数据
     */
    Map<String, Object> getJobStatistics(Long jobId);
    
    /**
     * 获取企业岗位统计
     * @param enterpriseId 企业ID
     * @return 统计数据
     */
    Map<String, Object> getEnterpriseJobStatistics(Long enterpriseId);
}