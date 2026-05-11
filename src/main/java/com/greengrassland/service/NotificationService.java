package com.greengrassland.service;

import com.greengrassland.dto.NotificationDTO;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 创建通知
     */
    void createNotification(String type, Long userId, Long postId, Long fromUserId, String content);

    /**
     * 获取用户的通知列表
     */
    List<NotificationDTO> getUserNotifications(Long userId);

    /**
     * 获取用户未读通知数
     */
    long getUnreadCount(Long userId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 标记单个通知为已读
     */
    void markAsRead(Long notificationId, Long userId);
}
