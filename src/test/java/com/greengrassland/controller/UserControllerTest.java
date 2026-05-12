package com.greengrassland.controller;

import com.greengrassland.dto.ApiResponse;
import com.greengrassland.dto.UserDTO;
import com.greengrassland.dto.UserRegisterDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testRegisterAndLogin() {
        UserRegisterDTO reg = new UserRegisterDTO();
        reg.setUsername("ctrluser");
        reg.setPassword("123456");
        reg.setNickname("CtrlUser");

        ResponseEntity<ApiResponse> regResp = restTemplate.postForEntity("/api/user/register", reg, ApiResponse.class);
        assertEquals(200, regResp.getStatusCodeValue());
        assertEquals(200, regResp.getBody().getCode());
    }

    @Test
    void testLogin() {
        String json = "{\"username\":\"tester\",\"password\":\"123456\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<ApiResponse> resp = restTemplate.exchange("/api/user/login", HttpMethod.POST,
                new HttpEntity<>(json, headers), ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testLoginWrongPassword() {
        String json = "{\"username\":\"tester\",\"password\":\"wrong\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<ApiResponse> resp = restTemplate.exchange("/api/user/login", HttpMethod.POST,
                new HttpEntity<>(json, headers), ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
        assertNotEquals(200, resp.getBody().getCode());
    }

    @Test
    void testGetUserById() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/user/1", ApiResponse.class);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testGetNonExistentUser() {
        ResponseEntity<ApiResponse> resp = restTemplate.getForEntity("/api/user/99999", ApiResponse.class);
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
