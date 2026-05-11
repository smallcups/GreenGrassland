package com.greengrassland.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO {

    private Long id;
    private Long roomId;
    private Long senderId;
    private Long receiverId;
    private String senderNickname;
    private String senderAvatar;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;
}
