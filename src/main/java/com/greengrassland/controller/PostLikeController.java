package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.service.PostLikeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞控制器
 */
@RestController
@RequestMapping("/api/post/like")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService likeService;

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<?>> toggleLike(@PathVariable Long postId,
                                                      HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        likeService.toggleLike(postId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
