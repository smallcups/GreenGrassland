package com.greengrassland.service;

import com.greengrassland.dto.GroupChatDTO;
import com.greengrassland.dto.GroupChatRoomDTO;
import com.greengrassland.dto.GroupChatSendDTO;

import java.util.List;

/**
 * 群聊服务接口
 */
public interface GroupChatService {

    /**
     * 获取活动的群聊房间（如果不存在则创建）
     */
    GroupChatRoomDTO getOrCreateGroupChatRoom(Long postId, Long userId);

    /**
     * 获取群聊消息列表
     */
    List<GroupChatDTO> getGroupChatMessages(Long roomId);

    /**
     * 发送群消息
     */
    GroupChatDTO sendGroupMessage(Long senderId, GroupChatSendDTO sendDTO);

    /**
     * 获取用户参与的群聊列表
     */
    List<GroupChatRoomDTO> getUserGroupChatRooms(Long userId);
}
