package com.greengrassland.service.impl;

import com.greengrassland.dto.GroupChatDTO;
import com.greengrassland.dto.GroupChatRoomDTO;
import com.greengrassland.dto.GroupChatSendDTO;
import com.greengrassland.entity.GroupChat;
import com.greengrassland.entity.GroupChatRoom;
import com.greengrassland.entity.Post;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.repository.GroupChatRepository;
import com.greengrassland.repository.GroupChatRoomRepository;
import com.greengrassland.repository.PostRegistrationRepository;
import com.greengrassland.repository.PostRepository;
import com.greengrassland.repository.UserRepository;
import com.greengrassland.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 群聊服务实现
 */
@Service
@RequiredArgsConstructor
public class GroupChatServiceImpl implements GroupChatService {

    private final GroupChatRoomRepository groupChatRoomRepository;
    private final GroupChatRepository groupChatRepository;
    private final PostRepository postRepository;
    private final PostRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GroupChatRoomDTO getOrCreateGroupChatRoom(Long postId, Long userId) {
        // 检查活动是否存在
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            throw new BusinessException("活动不存在");
        }

        // 检查用户是否已报名该活动
        boolean isRegistered = registrationRepository.existsByPostIdAndUserId(postId, userId);
        if (!isRegistered && !postOpt.get().getUserId().equals(userId)) {
            throw new BusinessException("需要先报名该活动才能加入群聊");
        }

        // 查找或创建群聊房间
        GroupChatRoom room = groupChatRoomRepository.findByPostId(postId)
                .orElseGet(() -> {
                    GroupChatRoom newRoom = GroupChatRoom.builder()
                            .postId(postId)
                            .creatorId(userId)
                            .build();
                    return groupChatRoomRepository.save(newRoom);
                });

        Post post = postOpt.get();
        return convertToRoomDTO(room, post.getTitle());
    }

    @Override
    public List<GroupChatDTO> getGroupChatMessages(Long roomId) {
        List<GroupChat> chats = groupChatRepository.findByRoomIdOrderByCreateTimeAsc(roomId);
        return chats.stream()
                .map(this::convertToChatDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GroupChatDTO sendGroupMessage(Long senderId, GroupChatSendDTO sendDTO) {
        // 检查房间是否存在
        Optional<GroupChatRoom> roomOpt = groupChatRoomRepository.findById(sendDTO.getRoomId());
        if (roomOpt.isEmpty()) {
            throw new BusinessException("群聊房间不存在");
        }

        GroupChatRoom room = roomOpt.get();

        // 检查用户是否已报名该活动
        boolean isRegistered = registrationRepository.existsByPostIdAndUserId(room.getPostId(), senderId);
        Optional<Post> postOpt = postRepository.findById(room.getPostId());
        boolean isCreator = postOpt.map(p -> p.getUserId().equals(senderId)).orElse(false);
        if (!isRegistered && !isCreator) {
            throw new BusinessException("需要先报名该活动才能发送消息");
        }

        // 创建消息
        GroupChat chat = GroupChat.builder()
                .roomId(room.getId())
                .senderId(senderId)
                .content(sendDTO.getContent())
                .build();

        chat = groupChatRepository.save(chat);

        // 更新房间的最后消息时间
        room.setLastMessageTime(chat.getCreateTime());
        groupChatRoomRepository.save(room);

        return convertToChatDTO(chat);
    }

    @Override
    public List<GroupChatRoomDTO> getUserGroupChatRooms(Long userId) {
        // 获取用户报名或发布的活动ID列表
        List<Long> postIds = registrationRepository.findPostIdsByUserId(userId);
        
        // 添加用户发布的活动
        List<Post> myPosts = postRepository.findByUserIdOrderByCreateTimeDesc(userId);
        myPosts.forEach(post -> {
            if (!postIds.contains(post.getId())) {
                postIds.add(post.getId());
            }
        });

        // 查找这些活动的群聊房间
        return postIds.stream()
                .flatMap(postId -> groupChatRoomRepository.findByPostId(postId).stream())
                .map(room -> {
                    Optional<Post> postOpt = postRepository.findById(room.getPostId());
                    String postTitle = postOpt.map(Post::getTitle).orElse("未知活动");
                    return convertToRoomDTO(room, postTitle);
                })
                .collect(Collectors.toList());
    }

    /**
     * 转换为房间DTO
     */
    private GroupChatRoomDTO convertToRoomDTO(GroupChatRoom room, String postTitle) {
        // 获取最后一条消息
        GroupChat lastChat = groupChatRepository.findFirstByRoomIdOrderByCreateTimeDesc(room.getId());
        String lastMessage = lastChat != null ? lastChat.getContent() : "";
        if (lastMessage.length() > 50) {
            lastMessage = lastMessage.substring(0, 50) + "...";
        }

        return GroupChatRoomDTO.builder()
                .id(room.getId())
                .postId(room.getPostId())
                .postTitle(postTitle)
                .lastMessage(lastMessage)
                .lastMessageTime(room.getLastMessageTime())
                .build();
    }

    /**
     * 转换为消息DTO
     */
    private GroupChatDTO convertToChatDTO(GroupChat chat) {
        Optional<com.greengrassland.entity.User> senderOpt = userRepository.findById(chat.getSenderId());
        String senderNickname = senderOpt.map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                .orElse("未知用户");
        String senderAvatar = senderOpt.map(com.greengrassland.entity.User::getAvatar).orElse(null);

        return GroupChatDTO.builder()
                .id(chat.getId())
                .roomId(chat.getRoomId())
                .senderId(chat.getSenderId())
                .senderNickname(senderNickname)
                .senderAvatar(senderAvatar)
                .content(chat.getContent())
                .createTime(chat.getCreateTime())
                .build();
    }
}
