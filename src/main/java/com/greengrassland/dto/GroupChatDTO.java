package com.greengrassland.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 群聊消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatDTO {

    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String senderAvatar;
    private String content;
    private LocalDateTime createTime;
}
