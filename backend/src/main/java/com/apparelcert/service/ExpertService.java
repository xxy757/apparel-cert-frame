package com.apparelcert.service;

import com.apparelcert.entity.Expert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 评审专家服务接口
 */
public interface ExpertService extends IService<Expert> {

    /**
     * 添加专家
     */
    boolean addExpert(Expert expert);

    /**
     * 更新专家信息
     */
    boolean updateExpert(Expert expert);

    /**
     * 删除专家
     */
    boolean deleteExpert(Long expertId);

    /**
     * 获取专家详情
     */
    Expert getExpertDetail(Long expertId);

    /**
     * 分页查询专家列表
     */
    Page<Expert> pageQuery(Integer page, Integer size, String keyword, String specialty, Integer status);

    /**
     * 根据专业领域获取专家列表
     */
    List<Expert> getExpertsBySpecialty(String specialty);

    /**
     * 启用/禁用专家
     */
    boolean updateExpertStatus(Long expertId, Integer status);

    /**
     * 获取可用专家列表（用于分配评审任务）
     */
    List<Expert> getAvailableExperts(String specialty, Integer reviewLevel);

    /**
     * 更新专家评审统计
     */
    boolean updateReviewCount(Long expertId);
}
