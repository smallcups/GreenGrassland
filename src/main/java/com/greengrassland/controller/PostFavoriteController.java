package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.service.PostFavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/post/favorite")
@RequiredArgsConstructor
public class PostFavoriteController {

    private final PostFavoriteService favoriteService;

    /**
     * 收藏/取消收藏
     */
    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<?>> toggleFavorite(@PathVariable Long postId,
                                                         HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        favoriteService.toggleFavorite(postId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 获取用户收藏的帖子列表
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<java.util.List<com.greengrassland.dto.PostDTO>>> getMyFavorites(HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        java.util.List<com.greengrassland.dto.PostDTO> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }
}
