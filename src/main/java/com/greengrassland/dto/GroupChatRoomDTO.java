package com.greengrassland.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 群聊房间DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatRoomDTO {

    private Long id;
    private Long postId;
    private String postTitle;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}
