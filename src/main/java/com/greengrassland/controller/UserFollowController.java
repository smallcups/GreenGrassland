package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.dto.UserDTO;
import com.greengrassland.service.UserFollowService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户关注控制器
 */
@RestController
@RequestMapping("/api/user/follow")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;

    /**
     * 关注/取消关注
     */
    @PostMapping("/{followingId}")
    public ResponseEntity<ApiResponse<?>> toggleFollow(@PathVariable Long followingId,
                                                        HttpServletRequest request) {
        Long followerId = SessionConfig.getCurrentUserId(request);
        if (followerId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        userFollowService.toggleFollow(followerId, followingId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 获取粉丝列表
     */
    @GetMapping("/followers/{userId}")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getFollowers(@PathVariable Long userId) {
        List<UserDTO> followers = userFollowService.getFollowers(userId);
        return ResponseEntity.ok(ApiResponse.success(followers));
    }

    /**
     * 获取关注列表
     */
    @GetMapping("/followings/{userId}")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getFollowings(@PathVariable Long userId) {
        List<UserDTO> followings = userFollowService.getFollowings(userId);
        return ResponseEntity.ok(ApiResponse.success(followings));
    }

    /**
     * 获取粉丝数
     */
    @GetMapping("/followers/count/{userId}")
    public ResponseEntity<ApiResponse<Long>> getFollowerCount(@PathVariable Long userId) {
        long count = userFollowService.getFollowerCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * 获取关注数
     */
    @GetMapping("/followings/count/{userId}")
    public ResponseEntity<ApiResponse<Long>> getFollowingCount(@PathVariable Long userId) {
        long count = userFollowService.getFollowingCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * 检查是否已关注
     */
    @GetMapping("/check/{followingId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFollowing(@PathVariable Long followingId,
                                                                HttpServletRequest request) {
        Long followerId = SessionConfig.getCurrentUserId(request);
        if (followerId == null) {
            return ResponseEntity.ok(ApiResponse.success(false));
        }

        boolean isFollowing = userFollowService.isFollowing(followerId, followingId);
        return ResponseEntity.ok(ApiResponse.success(isFollowing));
    }
}
