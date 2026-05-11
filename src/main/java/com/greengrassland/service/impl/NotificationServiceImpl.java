package com.greengrassland.service.impl;

import com.greengrassland.dto.NotificationDTO;
import com.greengrassland.entity.Notification;
import com.greengrassland.entity.User;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.repository.NotificationRepository;
import com.greengrassland.repository.UserRepository;
import com.greengrassland.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 通知服务实现
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createNotification(String type, Long userId, Long postId, Long fromUserId, String content) {
        // 不给自己发通知
        if (fromUserId != null && fromUserId.equals(userId)) {
            return;
        }

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .postId(postId)
                .fromUserId(fromUserId)
                .content(content)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationDTO> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreateTimeDesc(userId);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            throw new BusinessException("通知不存在");
        }

        Notification notification = notificationOpt.get();
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该通知");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * 转换为DTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO.NotificationDTOBuilder builder = NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .postId(notification.getPostId())
                .fromUserId(notification.getFromUserId())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .createTime(notification.getCreateTime());

        // 如果有触发通知的用户，获取用户信息
        if (notification.getFromUserId() != null) {
            Optional<User> userOpt = userRepository.findById(notification.getFromUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                builder.fromUserNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                builder.fromUserAvatar(user.getAvatar());
            }
        }

        return builder.build();
    }
}
