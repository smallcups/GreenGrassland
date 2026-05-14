package com.greengrassland.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String bio;
    private String interestTags;
    private Long postCount;
    private Long likeCount;
    private Long favoriteCount;
    private Long followerCount;
    private Long followingCount;
    private String token;
    private LocalDateTime createTime;
}
