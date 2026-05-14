package com.greengrassland.controller;

import com.greengrassland.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketOnlineController {

    private final OnlineStatusService onlineStatusService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/online.ping")
    @SendTo("/topic/online.status")
    public Map<String, Object> ping(Map<String, Object> payload) {
        Object userIdObj = payload.get("userId");
        if (userIdObj != null) {
            Long userId = userIdObj instanceof Number ? ((Number) userIdObj).longValue() : Long.valueOf(userIdObj.toString());
            String sessionId = (String) payload.getOrDefault("sessionId", "default");
            onlineStatusService.userConnected(userId, sessionId);
            return Map.of("userId", userId, "online", true);
        }
        return Map.of("online", false);
    }

    @MessageMapping("/typing")
    public void typing(Map<String, Object> payload) {
        Object roomIdObj = payload.get("roomId");
        Object typeObj = payload.get("type");
        Object userIdObj = payload.get("userId");
        Object nicknameObj = payload.get("nickname");
        if (roomIdObj == null || typeObj == null) return;
        String roomId = roomIdObj.toString();
        String roomType = typeObj.toString();
        String topic = "group".equals(roomType) ? "/topic/group.typing." + roomId : "/topic/chat.typing." + roomId;
        messagingTemplate.convertAndSend(topic,
                Map.of("userId", userIdObj != null ? userIdObj : 0,
                       "nickname", nicknameObj != null ? nicknameObj : "",
                       "typing", true));
    }
}
