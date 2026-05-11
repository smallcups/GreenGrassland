package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.dto.ChatDTO;
import com.greengrassland.dto.ChatRoomDTO;
import com.greengrassland.dto.ChatSendDTO;
import com.greengrassland.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 私聊控制器
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 获取聊天房间列表
     */
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomDTO>>> getChatRooms(HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        List<ChatRoomDTO> rooms = chatService.getChatRooms(userId);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    /**
     * 获取聊天消息
     */
    @GetMapping("/room/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatDTO>>> getChatMessages(@PathVariable Long roomId,
                                                                       HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        List<ChatDTO> messages = chatService.getChatMessages(roomId, userId);
        // 标记为已读
        chatService.markAsRead(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatDTO>> sendMessage(@Valid @RequestBody ChatSendDTO sendDTO,
                                                             HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        ChatDTO chatDTO = chatService.sendMessage(userId, sendDTO);
        return ResponseEntity.ok(ApiResponse.success(chatDTO));
    }

    /**
     * 获取或创建与指定用户的聊天房间
     */
    @PostMapping("/room/with/{otherUserId}")
    public ResponseEntity<ApiResponse<ChatRoomDTO>> getOrCreateChatRoom(@PathVariable Long otherUserId,
                                                                        HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        ChatRoomDTO room = chatService.getOrCreateChatRoom(userId, otherUserId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }
}
