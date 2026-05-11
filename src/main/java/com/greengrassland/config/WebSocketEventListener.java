package com.greengrassland.config;

import com.greengrassland.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final OnlineStatusService onlineStatusService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        // User ID is captured via handshake interceptor if session is available
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Map<String, Object> attrs = event.getMessage().getHeaders()
                .get("simpSessionAttributes", Map.class);
        if (attrs != null && attrs.get("userId") != null) {
            Long userId = (Long) attrs.get("userId");
            String sessionId = event.getSessionId();
            onlineStatusService.userDisconnected(userId, sessionId);
            messagingTemplate.convertAndSend("/topic/online.status",
                    Map.of("userId", userId, "online", false));
        }
    }
}
