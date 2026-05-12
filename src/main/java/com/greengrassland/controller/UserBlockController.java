package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.entity.UserBlock;
import com.greengrassland.repository.UserBlockRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/block")
@RequiredArgsConstructor
public class UserBlockController {

    private final UserBlockRepository userBlockRepository;

    @PostMapping("/{blockedUserId}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> blockUser(@PathVariable Long blockedUserId, HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) return ResponseEntity.ok(ApiResponse.error("请先登录"));
        if (userId.equals(blockedUserId)) return ResponseEntity.ok(ApiResponse.error("不能屏蔽自己"));
        if (userBlockRepository.existsByUserIdAndBlockedUserId(userId, blockedUserId)) {
            return ResponseEntity.ok(ApiResponse.error("已屏蔽该用户"));
        }
        userBlockRepository.save(UserBlock.builder().userId(userId).blockedUserId(blockedUserId).build());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{blockedUserId}")
    @Transactional
    public ResponseEntity<ApiResponse<?>> unblockUser(@PathVariable Long blockedUserId, HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) return ResponseEntity.ok(ApiResponse.error("请先登录"));
        userBlockRepository.deleteByUserIdAndBlockedUserId(userId, blockedUserId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/check/{blockedUserId}")
    public ResponseEntity<ApiResponse<Boolean>> checkBlock(@PathVariable Long blockedUserId, HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) return ResponseEntity.ok(ApiResponse.success(false));
        return ResponseEntity.ok(ApiResponse.success(userBlockRepository.existsByUserIdAndBlockedUserId(userId, blockedUserId)));
    }
}
