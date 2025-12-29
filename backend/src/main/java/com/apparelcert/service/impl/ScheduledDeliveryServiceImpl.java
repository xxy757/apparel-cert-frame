package com.apparelcert.service.impl;

import com.apparelcert.entity.ScheduledDelivery;
import com.apparelcert.mapper.ScheduledDeliveryMapper;
import com.apparelcert.service.ApplicationService;
import com.apparelcert.service.ScheduledDeliveryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 定时投递服务实现类
 */
@Service
public class ScheduledDeliveryServiceImpl extends ServiceImpl<ScheduledDeliveryMapper, ScheduledDelivery> 
        implements ScheduledDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledDeliveryServiceImpl.class);

    @Autowired
    private ScheduledDeliveryMapper deliveryMapper;
    
    @Autowired(required = false)
    private ApplicationService applicationService;

    @Override
    public boolean createScheduledDelivery(Long userId, Long jobId, Long resumeId, Date scheduledTime) {
        // 检查是否已存在相同的定时投递
        QueryWrapper<ScheduledDelivery> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("job_id", jobId);
        wrapper.eq("status", 0); // 待执行
        if (this.count(wrapper) > 0) {
            return false; // 已存在待执行的定时投递
        }
        
        ScheduledDelivery delivery = new ScheduledDelivery();
        delivery.setUserId(userId);
        delivery.setJobId(jobId);
        delivery.setResumeId(resumeId);
        delivery.setScheduledTime(scheduledTime);
        delivery.setStatus(0); // 待执行
        delivery.setCreateTime(new Date());
        
        return this.save(delivery);
    }

    @Override
    public boolean cancelScheduledDelivery(Long deliveryId, Long userId) {
        ScheduledDelivery delivery = this.getById(deliveryId);
        if (delivery == null || !delivery.getUserId().equals(userId)) {
            return false;
        }
        if (delivery.getStatus() != 0) {
            return false; // 只能取消待执行的任务
        }
        
        delivery.setStatus(2); // 已取消
        delivery.setUpdateTime(new Date());
        return this.updateById(delivery);
    }

    @Override
    public Page<ScheduledDelivery> getUserScheduledDeliveries(Long userId, Integer page, Integer size, Integer status) {
        Page<ScheduledDelivery> pageInfo = new Page<>(page, size);
        QueryWrapper<ScheduledDelivery> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return deliveryMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public List<ScheduledDelivery> getPendingDeliveries() {
        QueryWrapper<ScheduledDelivery> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0); // 待执行
        wrapper.le("scheduled_time", new Date()); // 已到执行时间
        wrapper.orderByAsc("scheduled_time");
        return deliveryMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeScheduledDelivery(ScheduledDelivery delivery) {
        try {
            // 执行投递
            if (applicationService != null) {
                // applyJob参数顺序: jobId, userId, resumeUrl
                boolean success = applicationService.applyJob(delivery.getJobId(), 
                        delivery.getUserId(), String.valueOf(delivery.getResumeId()));
                
                delivery.setExecutedTime(new Date());
                delivery.setUpdateTime(new Date());
                
                if (success) {
                    delivery.setStatus(1); // 已执行
                    delivery.setResultMessage("投递成功");
                } else {
                    delivery.setStatus(3); // 执行失败
                    delivery.setResultMessage("投递失败，可能已投递过该岗位");
                }
            } else {
                delivery.setStatus(3);
                delivery.setResultMessage("投递服务不可用");
            }
            
            this.updateById(delivery);
            logger.info("定时投递执行完成: deliveryId={}, status={}", delivery.getId(), delivery.getStatus());
            
        } catch (Exception e) {
            logger.error("定时投递执行异常: deliveryId={}, error={}", delivery.getId(), e.getMessage());
            delivery.setStatus(3);
            delivery.setResultMessage("执行异常: " + e.getMessage());
            delivery.setExecutedTime(new Date());
            delivery.setUpdateTime(new Date());
            this.updateById(delivery);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchCreateScheduledDelivery(Long userId, List<Long> jobIds, Long resumeId, Date scheduledTime) {
        int count = 0;
        for (Long jobId : jobIds) {
            if (createScheduledDelivery(userId, jobId, resumeId, scheduledTime)) {
                count++;
            }
        }
        return count;
    }
}
