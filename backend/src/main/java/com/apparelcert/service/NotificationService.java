package com.apparelcert.service;

import com.apparelcert.entity.Notification;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 消息通知服务接口
 */
public interface NotificationService {

    /**
     * 发送通知
     * @param userId 用户ID
     * @param userType 用户类型 (1:个人用户, 2:企业用户, 3:专家)
     * @param title 通知标题
     * @param content 通知内容
     * @return 是否发送成功
     */
    boolean sendNotification(Long userId, Integer userType, String title, String content);

    /**
     * 批量发送通知
     * @param userIds 用户ID列表
     * @param userType 用户类型
     * @param title 通知标题
     * @param content 通知内容
     * @return 是否发送成功
     */
    boolean sendBatchNotification(List<Long> userIds, Integer userType, String title, String content);

    /**
     * 分页查询用户消息列表
     * @param userId 用户ID
     * @param userType 用户类型
     * @param page 分页参数
     * @return 消息分页列表
     */
    IPage<Notification> getNotificationList(Long userId, Integer userType, Page<Notification> page);

    /**
     * 标记消息为已读
     * @param notificationId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有消息为已读
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 是否成功
     */
    boolean markAllAsRead(Long userId, Integer userType);

    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 未读消息数量
     */
    int getUnreadCount(Long userId, Integer userType);

    /**
     * 删除消息
     * @param notificationId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteNotification(Long notificationId, Long userId);

    /**
     * 获取消息详情
     * @param notificationId 消息ID
     * @return 消息详情
     */
    Notification getNotificationById(Long notificationId);
}

