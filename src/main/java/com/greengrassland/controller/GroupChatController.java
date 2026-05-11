package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.dto.GroupChatDTO;
import com.greengrassland.dto.GroupChatRoomDTO;
import com.greengrassland.dto.GroupChatSendDTO;
import com.greengrassland.service.GroupChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群聊控制器
 */
@RestController
@RequestMapping("/api/group-chat")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;

    /**
     * 获取或创建活动的群聊房间
     */
    @GetMapping("/post/{postId}/room")
    public ResponseEntity<ApiResponse<GroupChatRoomDTO>> getOrCreateRoom(@PathVariable Long postId,
                                                                          HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        GroupChatRoomDTO room = groupChatService.getOrCreateGroupChatRoom(postId, userId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    /**
     * 获取群聊消息
     */
    @GetMapping("/room/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<GroupChatDTO>>> getGroupChatMessages(@PathVariable Long roomId) {
        List<GroupChatDTO> messages = groupChatService.getGroupChatMessages(roomId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * 发送群消息
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<GroupChatDTO>> sendGroupMessage(@Valid @RequestBody GroupChatSendDTO sendDTO,
                                                                       HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        GroupChatDTO chatDTO = groupChatService.sendGroupMessage(userId, sendDTO);
        return ResponseEntity.ok(ApiResponse.success(chatDTO));
    }

    /**
     * 获取用户的群聊列表
     */
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<GroupChatRoomDTO>>> getUserGroupChatRooms(HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        List<GroupChatRoomDTO> rooms = groupChatService.getUserGroupChatRooms(userId);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }
}
