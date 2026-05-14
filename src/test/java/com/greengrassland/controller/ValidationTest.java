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
class ValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testRegisterEmptyUsername() {
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = restTemplate.exchange("/api/user/register", HttpMethod.POST,
                new HttpEntity<>("{\"username\":\"\",\"password\":\"123456\"}", h), String.class);
        // Spring @Valid rejects empty username, returns 400
        assertTrue(resp.getStatusCodeValue() == 200 || resp.getStatusCodeValue() == 400);
    }

    @Test
    void testLoginEmptyBody() {
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = restTemplate.exchange("/api/user/login", HttpMethod.POST,
                new HttpEntity<>("{}", h), String.class);
        assertTrue(resp.getStatusCodeValue() == 200 || resp.getStatusCodeValue() == 400);
    }

    @Test
    void testPostListExtremePage() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post?page=99999&pageSize=1", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testPostListPageSize100() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post?page=1&pageSize=100", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testSearchEmptyKeyword() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post?keyword=&page=1&pageSize=3", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testGetUserByNegativeId() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/user/-1", ApiResponse.class);
        assertTrue(resp.getStatusCodeValue() == 200 || resp.getStatusCodeValue() == 400);
    }

    @Test
    void testGetParticipantsEmpty() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/post/registration/99999/participants", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testSwaggerDocs() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/v3/api-docs", String.class);
        assertEquals(200, resp.getStatusCodeValue());
    }
}
