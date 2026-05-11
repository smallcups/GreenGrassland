package com.greengrassland.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private Long userId;
    private String type;
    private Long postId;
    private Long fromUserId;
    private String fromUserNickname;
    private String fromUserAvatar;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;
}
