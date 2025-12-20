package com.apparelcert.service;

import com.apparelcert.entity.PracticalTask;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 实操任务服务接口
 */
public interface PracticalTaskService extends IService<PracticalTask> {
    /**
     * 分页查询实操任务列表
     */
    Page<PracticalTask> pageQuery(Integer page, Integer size, String keyword, String type, Integer level);
    
    /**
     * 获取实操任务详情
     */
    PracticalTask getById(Long id);
    
    /**
     * 保存或更新实操任务
     */
    boolean saveOrUpdateTask(PracticalTask task);
    
    /**
     * 删除实操任务
     */
    boolean deleteTask(Long id);
    
    /**
     * 批量删除实操任务
     */
    boolean deleteBatchTasks(List<Long> ids);
    
    /**
     * 根据认证类型和等级获取实操任务
     */
    PracticalTask getByCertificationAndLevel(String type, Integer level);
    
    /**
     * 获取所有实操任务类型
     */
    List<String> getAllTypes();
    
    /**
     * 根据类型获取实操任务列表
     */
    List<PracticalTask> getByType(String type);
}