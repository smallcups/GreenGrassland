package com.greengrassland.service.impl;

import com.greengrassland.dto.ChatDTO;
import com.greengrassland.dto.ChatRoomDTO;
import com.greengrassland.dto.ChatSendDTO;
import com.greengrassland.entity.Chat;
import com.greengrassland.entity.ChatRoom;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.repository.ChatRepository;
import com.greengrassland.repository.ChatRoomRepository;
import com.greengrassland.repository.UserRepository;
import com.greengrassland.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 私聊服务实现
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @Override
    public List<ChatRoomDTO> getChatRooms(Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findByUserId(userId);
        return rooms.stream()
                .map(room -> convertToRoomDTO(room, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatDTO> getChatMessages(Long roomId, Long userId) {
        // 验证用户是否有权限访问该房间
        Optional<ChatRoom> roomOpt = chatRoomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            throw new BusinessException("聊天房间不存在");
        }

        ChatRoom room = roomOpt.get();
        if (!room.getUser1Id().equals(userId) && !room.getUser2Id().equals(userId)) {
            throw new BusinessException("无权访问该聊天房间");
        }

        List<Chat> chats = chatRepository.findByRoomIdOrderByCreateTimeAsc(roomId);
        return chats.stream()
                .map(this::convertToChatDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatDTO sendMessage(Long senderId, ChatSendDTO sendDTO) {
        Long receiverId = sendDTO.getReceiverId();

        // 查找或创建聊天房间
        ChatRoom room = chatRoomRepository.findByUser1IdAndUser2Id(senderId, receiverId)
                .orElseGet(() -> {
                    // 确保user1Id < user2Id
                    Long user1Id = senderId < receiverId ? senderId : receiverId;
                    Long user2Id = senderId < receiverId ? receiverId : senderId;
                    ChatRoom newRoom = ChatRoom.builder()
                            .user1Id(user1Id)
                            .user2Id(user2Id)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        // 创建消息
        Chat chat = Chat.builder()
                .roomId(room.getId())
                .senderId(senderId)
                .receiverId(receiverId)
                .content(sendDTO.getContent())
                .isRead(false)
                .build();

        chat = chatRepository.save(chat);

        // 更新房间的最后消息时间
        room.setLastMessageTime(chat.getCreateTime());
        chatRoomRepository.save(room);

        return convertToChatDTO(chat);
    }

    @Override
    @Transactional
    public void markAsRead(Long roomId, Long userId) {
        chatRepository.markAsReadByRoomIdAndReceiverId(roomId, userId);
    }

    @Override
    @Transactional
    public ChatRoomDTO getOrCreateChatRoom(Long currentUserId, Long otherUserId) {
        if (currentUserId.equals(otherUserId)) {
            throw new BusinessException("不能与自己聊天");
        }

        // 查找或创建聊天房间
        ChatRoom room = chatRoomRepository.findByUser1IdAndUser2Id(currentUserId, otherUserId)
                .orElseGet(() -> {
                    // 确保user1Id < user2Id
                    Long user1Id = currentUserId < otherUserId ? currentUserId : otherUserId;
                    Long user2Id = currentUserId < otherUserId ? otherUserId : currentUserId;
                    ChatRoom newRoom = ChatRoom.builder()
                            .user1Id(user1Id)
                            .user2Id(user2Id)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        return convertToRoomDTO(room, currentUserId);
    }

    /**
     * 转换为房间DTO
     */
    private ChatRoomDTO convertToRoomDTO(ChatRoom room, Long currentUserId) {
        // 确定对方用户ID
        Long otherUserId = room.getUser1Id().equals(currentUserId) ? room.getUser2Id() : room.getUser1Id();

        // 获取对方用户信息
        Optional<com.greengrassland.entity.User> otherUserOpt = userRepository.findById(otherUserId);
        String otherUserNickname = otherUserOpt.map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                .orElse("未知用户");
        String otherUserAvatar = otherUserOpt.map(com.greengrassland.entity.User::getAvatar).orElse(null);

        // 获取最后一条消息
        Chat lastChat = chatRepository.findFirstByRoomIdOrderByCreateTimeDesc(room.getId());
        String lastMessage = lastChat != null ? lastChat.getContent() : "";
        if (lastMessage.length() > 50) {
            lastMessage = lastMessage.substring(0, 50) + "...";
        }

        // 统计未读消息数
        long unreadCount = chatRepository.countByRoomIdAndReceiverIdAndIsReadFalse(room.getId(), currentUserId);

        return ChatRoomDTO.builder()
                .id(room.getId())
                .user1Id(room.getUser1Id())
                .user2Id(room.getUser2Id())
                .otherUserNickname(otherUserNickname)
                .otherUserAvatar(otherUserAvatar)
                .lastMessage(lastMessage)
                .lastMessageTime(room.getLastMessageTime())
                .unreadCount(unreadCount)
                .build();
    }

    /**
     * 转换为消息DTO
     */
    private ChatDTO convertToChatDTO(Chat chat) {
        Optional<com.greengrassland.entity.User> senderOpt = userRepository.findById(chat.getSenderId());
        String senderNickname = senderOpt.map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                .orElse("未知用户");
        String senderAvatar = senderOpt.map(com.greengrassland.entity.User::getAvatar).orElse(null);

        return ChatDTO.builder()
                .id(chat.getId())
                .roomId(chat.getRoomId())
                .senderId(chat.getSenderId())
                .receiverId(chat.getReceiverId())
                .senderNickname(senderNickname)
                .senderAvatar(senderAvatar)
                .content(chat.getContent())
                .isRead(chat.getIsRead())
                .createTime(chat.getCreateTime())
                .build();
    }
}
