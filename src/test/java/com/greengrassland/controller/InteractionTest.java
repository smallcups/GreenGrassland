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
class InteractionTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetComments() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/comment/post/1", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testGetNotifications() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/notification", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testUnreadCount() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/notification/unread/count", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testMarkAllRead() {
        ResponseEntity<ApiResponse> resp = restTemplate.postForEntity("/api/notification/mark-all-read", null, ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testGetFollowers() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/user/follow/followers/1", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testGetFollowings() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/user/follow/followings/1", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testFollowersCount() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/user/follow/followers/count/1", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testFollowingsCount() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/user/follow/followings/count/1", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testFollowCheck() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/user/follow/check/2", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testBlockCheck() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/user/block/check/2", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testOnlineStatus() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/online/status/1", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testMyFavorites() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post/favorite/my", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testRecommendEndpoint() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post/recommend", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testUploadNoFile() {
        ResponseEntity<ApiResponse> resp = restTemplate.postForEntity("/api/upload/avatar", null, ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
        assertNotEquals(200, resp.getBody().getCode());
    }

    @Test
    void testUploadPostImageNoFile() {
        ResponseEntity<ApiResponse> resp = restTemplate.postForEntity("/api/upload/post-image", null, ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
        assertNotEquals(200, resp.getBody().getCode());
    }

    @Test
    void testHealthCheck() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/actuator/health", String.class);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().contains("UP"));
    }
}
