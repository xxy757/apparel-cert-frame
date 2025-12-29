package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.ScheduledDelivery;
import com.apparelcert.service.ScheduledDeliveryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 定时投递控制器
 */
@RestController
@RequestMapping("/api/scheduled-delivery")
public class ScheduledDeliveryController {

    @Autowired
    private ScheduledDeliveryService scheduledDeliveryService;

    /**
     * 创建定时投递任务
     */
    @PostMapping("/create")
    public Result<Boolean> createScheduledDelivery(
            @RequestParam Long userId,
            @RequestParam Long jobId,
            @RequestParam Long resumeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date scheduledTime) {
        
        // 验证时间必须是未来时间
        if (scheduledTime.before(new Date())) {
            return Result.error(400, "定时投递时间必须是未来时间");
        }
        
        boolean result = scheduledDeliveryService.createScheduledDelivery(userId, jobId, resumeId, scheduledTime);
        return result ? Result.success(true) : Result.error(500, "创建失败，可能已存在相同的定时投递任务");
    }

    /**
     * 批量创建定时投递
     */
    @PostMapping("/batch-create")
    public Result<Integer> batchCreateScheduledDelivery(@RequestBody Map<String, Object> params) {
        Long userId = Long.parseLong(params.get("userId").toString());
        @SuppressWarnings("unchecked")
        List<Long> jobIds = ((List<Number>) params.get("jobIds"))
            .stream().map(Number::longValue).collect(java.util.stream.Collectors.toList());
        Long resumeId = Long.parseLong(params.get("resumeId").toString());
        
        // 解析时间
        String scheduledTimeStr = params.get("scheduledTime").toString();
        Date scheduledTime;
        try {
            scheduledTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(scheduledTimeStr);
        } catch (Exception e) {
            return Result.error(400, "时间格式错误");
        }
        
        if (scheduledTime.before(new Date())) {
            return Result.error(400, "定时投递时间必须是未来时间");
        }
        
        int count = scheduledDeliveryService.batchCreateScheduledDelivery(userId, jobIds, resumeId, scheduledTime);
        return Result.success(count);
    }

    /**
     * 取消定时投递
     */
    @PutMapping("/cancel")
    public Result<Boolean> cancelScheduledDelivery(@RequestParam Long deliveryId, @RequestParam Long userId) {
        boolean result = scheduledDeliveryService.cancelScheduledDelivery(deliveryId, userId);
        return result ? Result.success(true) : Result.error(500, "取消失败");
    }

    /**
     * 获取用户的定时投递列表
     */
    @GetMapping("/list")
    public Result<Page<ScheduledDelivery>> getUserScheduledDeliveries(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<ScheduledDelivery> pageInfo = scheduledDeliveryService.getUserScheduledDeliveries(userId, page, size, status);
        return Result.success(pageInfo);
    }

    /**
     * 获取定时投递详情
     */
    @GetMapping("/detail")
    public Result<ScheduledDelivery> getScheduledDeliveryDetail(@RequestParam Long deliveryId) {
        ScheduledDelivery delivery = scheduledDeliveryService.getById(deliveryId);
        return Result.success(delivery);
    }
}
