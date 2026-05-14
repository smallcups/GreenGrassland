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
class FollowCommentNotificationTest {

    @Autowired
    private TestRestTemplate restTemplate;

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
    void testGetPostListPagination() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post?page=2&pageSize=3", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testOnlineStatus() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/online/status/1", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }
}
