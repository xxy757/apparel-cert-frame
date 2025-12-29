package com.apparelcert.service;

import com.apparelcert.entity.ScheduledDelivery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

/**
 * 定时投递服务接口
 */
public interface ScheduledDeliveryService extends IService<ScheduledDelivery> {
    
    /**
     * 创建定时投递任务
     */
    boolean createScheduledDelivery(Long userId, Long jobId, Long resumeId, Date scheduledTime);
    
    /**
     * 取消定时投递
     */
    boolean cancelScheduledDelivery(Long deliveryId, Long userId);
    
    /**
     * 获取用户的定时投递列表
     */
    Page<ScheduledDelivery> getUserScheduledDeliveries(Long userId, Integer page, Integer size, Integer status);
    
    /**
     * 获取待执行的定时投递任务
     */
    List<ScheduledDelivery> getPendingDeliveries();
    
    /**
     * 执行定时投递
     */
    void executeScheduledDelivery(ScheduledDelivery delivery);
    
    /**
     * 批量创建定时投递
     */
    int batchCreateScheduledDelivery(Long userId, List<Long> jobIds, Long resumeId, Date scheduledTime);
}
