package com.greengrassland.controller;

import com.greengrassland.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatUploadTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetChatRooms() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/chat/rooms", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testGetGroupChatRooms() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/group-chat/rooms", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testUploadAvatarNoFile() {
        ResponseEntity<ApiResponse> resp = restTemplate.postForEntity("/api/upload/avatar", null, ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
        assertNotEquals(200, resp.getBody().getCode());
    }

    @Test
    void testBlockCheck() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/user/block/check/2", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testFollowCheck() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/user/follow/check/2", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testSearchByType() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post?type=BALL_GAME&page=1&pageSize=5", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testSortDesc() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post?sortBy=createTime&sortOrder=DESC&page=1&pageSize=3", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testMyFavorites() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post/favorite/my", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }
}
