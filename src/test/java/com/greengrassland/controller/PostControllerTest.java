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
class PostControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetPostList() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post?page=1&pageSize=3", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testSearchByKeyword() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post?keyword=test&page=1&pageSize=5", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testGetPostDetail() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post/1", ApiResponse.class);
        assertTrue(resp.getStatusCodeValue() == 200 || resp.getStatusCodeValue() == 400);
    }

    @Test
    void testGetNonExistentPost() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post/99999", ApiResponse.class);
        assertEquals(400, resp.getStatusCodeValue());
    }
}
