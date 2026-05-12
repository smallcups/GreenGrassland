package com.greengrassland.controller;

import com.greengrassland.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatGroupChatTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetChatRooms() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/chat/rooms", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testCreateRoomWithUser2() {
        ResponseEntity<ApiResponse> resp = restTemplate.postForEntity("/api/chat/room/with/2", null, ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testGetGroupChatRooms() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/group-chat/rooms", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testGetGroupChatRoom() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/group-chat/post/1/room", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }
}
