package com.greengrassland.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天房间DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {

    private Long id;
    private Long user1Id;
    private Long user2Id;
    private String otherUserNickname;
    private String otherUserAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
}
