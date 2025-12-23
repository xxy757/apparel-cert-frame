package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Notification;
import com.apparelcert.service.NotificationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 分页查询消息列表
     * @param userId 用户ID
     * @param userType 用户类型
     * @param current 当前页
     * @param size 每页大小
     * @return 消息列表
     */
    @GetMapping("/list")
    public Result<IPage<Notification>> getNotificationList(
            @RequestParam Long userId,
            @RequestParam Integer userType,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Notification> page = new Page<>(current, size);
        IPage<Notification> result = notificationService.getNotificationList(userId, userType, page);
        return Result.success(result);
    }

    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@RequestParam Long userId, @RequestParam Integer userType) {
        int count = notificationService.getUnreadCount(userId, userType);
        return Result.success(count);
    }

    /**
     * 标记消息为已读
     * @param notificationId 消息ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/read/{notificationId}")
    public Result<Boolean> markAsRead(@PathVariable Long notificationId, @RequestParam Long userId) {
        boolean success = notificationService.markAsRead(notificationId, userId);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("Message not found or already read");
        }
    }

    /**
     * 标记所有消息为已读
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 操作结果
     */
    @PutMapping("/read-all")
    public Result<Boolean> markAllAsRead(@RequestParam Long userId, @RequestParam Integer userType) {
        boolean success = notificationService.markAllAsRead(userId, userType);
        return Result.success(success);
    }

    /**
     * 删除消息
     * @param notificationId 消息ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{notificationId}")
    public Result<Boolean> deleteNotification(@PathVariable Long notificationId, @RequestParam Long userId) {
        boolean success = notificationService.deleteNotification(notificationId, userId);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("Failed to delete notification");
        }
    }

    /**
     * 获取消息详情
     * @param notificationId 消息ID
     * @return 消息详情
     */
    @GetMapping("/{notificationId}")
    public Result<Notification> getNotificationById(@PathVariable Long notificationId) {
        Notification notification = notificationService.getNotificationById(notificationId);
        if (notification != null) {
            return Result.success(notification);
        } else {
            return Result.error("Notification not found");
        }
    }

    /**
     * 发送通知（内部使用或测试）
     * @param params 包含userId, userType, title, content的参数
     * @return 操作结果
     */
    @PostMapping("/send")
    public Result<Boolean> sendNotification(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer userType = Integer.valueOf(params.get("userType").toString());
        String title = params.get("title").toString();
        String content = params.get("content").toString();
        boolean success = notificationService.sendNotification(userId, userType, title, content);
        return Result.success(success);
    }
}

