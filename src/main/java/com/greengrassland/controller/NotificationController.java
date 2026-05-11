package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.dto.NotificationDTO;
import com.greengrassland.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取当前用户的通知列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotifications(HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * 获取未读通知数
     */
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.success(0L));
        }

        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<?>> markAllAsRead(HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 标记单个通知为已读
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<?>> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
