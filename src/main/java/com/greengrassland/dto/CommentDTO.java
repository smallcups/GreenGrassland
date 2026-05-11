package com.greengrassland.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String content;
    private Long parentCommentId; // 父评论ID
    private String parentCommentUsername; // 父评论用户名（用于显示"回复@xxx"）
    private java.util.List<CommentDTO> replies; // 子评论列表
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
