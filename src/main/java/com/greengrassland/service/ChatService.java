package com.greengrassland.service;

import com.greengrassland.dto.ChatDTO;
import com.greengrassland.dto.ChatRoomDTO;
import com.greengrassland.dto.ChatSendDTO;

import java.util.List;

/**
 * 私聊服务接口
 */
public interface ChatService {

    /**
     * 获取用户的聊天房间列表
     */
    List<ChatRoomDTO> getChatRooms(Long userId);

    /**
     * 获取聊天消息列表
     */
    List<ChatDTO> getChatMessages(Long roomId, Long userId);

    /**
     * 发送消息
     */
    ChatDTO sendMessage(Long senderId, ChatSendDTO sendDTO);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long roomId, Long userId);

    /**
     * 获取或创建与指定用户的聊天房间
     */
    ChatRoomDTO getOrCreateChatRoom(Long currentUserId, Long otherUserId);
}
