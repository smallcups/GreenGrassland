package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.service.PostRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 活动报名控制器
 */
@RestController
@RequestMapping("/api/post/registration")
@RequiredArgsConstructor
public class PostRegistrationController {

    private final PostRegistrationService registrationService;

    /**
     * 报名活动
     */
    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<?>> registerPost(@PathVariable Long postId,
                                                        HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        registrationService.registerPost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 取消报名
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<?>> cancelRegistration(@PathVariable Long postId,
                                                              HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        registrationService.cancelRegistration(postId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
