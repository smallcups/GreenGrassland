package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.dto.UserDTO;
import com.greengrassland.entity.User;
import com.greengrassland.repository.UserFollowRepository;
import com.greengrassland.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/common")
@RequiredArgsConstructor
public class CommonFollowController {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;

    @GetMapping("/follows/{otherUserId}")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getCommonFollows(@PathVariable Long otherUserId,
                                                                        HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) return ResponseEntity.ok(ApiResponse.error("请先登录"));

        Set<Long> myFollowings = userFollowRepository.findByFollowerId(userId)
                .stream().map(f -> f.getFollowingId()).collect(Collectors.toSet());
        Set<Long> theirFollowings = userFollowRepository.findByFollowerId(otherUserId)
                .stream().map(f -> f.getFollowingId()).collect(Collectors.toSet());
        myFollowings.retainAll(theirFollowings);

        List<User> users = userRepository.findAllById(myFollowings);
        List<UserDTO> dtos = users.stream().map(u -> UserDTO.builder()
                .id(u.getId()).username(u.getUsername())
                .nickname(u.getNickname()).avatar(u.getAvatar()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
