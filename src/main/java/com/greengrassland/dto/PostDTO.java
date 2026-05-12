package com.greengrassland.dto;

import com.greengrassland.entity.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {

    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String userAvatar; // 发布者头像
    private String title;
    private String content;
    private PostType type;
    private Integer maxPeople;
    private Integer currentPeople;
    private LocalDateTime activityTime;
    private String location;
    private String images;
    private Double latitude;
    private Double longitude;
    private Double distance;
    private Boolean approvalMode;
    private Boolean isSeries;
    private com.greengrassland.entity.PostStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isRegistered; // 当前用户是否已报名
    private Boolean isLiked; // 当前用户是否已点赞
    private Boolean isFavorited; // 当前用户是否已收藏
    private Integer likeCount; // 点赞数
    private Integer favoriteCount; // 收藏数
    private Integer commentCount; // 评论数
    private java.util.List<CommentDTO> comments; // 评论列表
}
