package com.apparelcert.service.impl;

import com.apparelcert.entity.Notification;
import com.apparelcert.mapper.NotificationMapper;
import com.apparelcert.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 消息通知服务实现类
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public boolean sendNotification(Long userId, Integer userType, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setUserType(userType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        return notificationMapper.insert(notification) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendBatchNotification(List<Long> userIds, Integer userType, String title, String content) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }
        List<Notification> notifications = new ArrayList<>();
        Date now = new Date();
        for (Long userId : userIds) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setUserType(userType);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setIsRead(0);
            notification.setCreateTime(now);
            notifications.add(notification);
        }
        // 逐条插入（MyBatis-Plus需要配置批量插入或使用循环）
        for (Notification notification : notifications) {
            notificationMapper.insert(notification);
        }
        return true;
    }

    @Override
    public IPage<Notification> getNotificationList(Long userId, Integer userType, Page<Notification> page) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getUserType, userType)
                .orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean markAsRead(Long notificationId, Long userId) {
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getId, notificationId)
                .eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, 1);
        return notificationMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean markAllAsRead(Long userId, Integer userType) {
        return notificationMapper.markAllAsRead(userId, userType) >= 0;
    }

    @Override
    public int getUnreadCount(Long userId, Integer userType) {
        return notificationMapper.countUnread(userId, userType);
    }

    @Override
    public boolean deleteNotification(Long notificationId, Long userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getId, notificationId)
                .eq(Notification::getUserId, userId);
        return notificationMapper.delete(queryWrapper) > 0;
    }

    @Override
    public Notification getNotificationById(Long notificationId) {
        return notificationMapper.selectById(notificationId);
    }
}

