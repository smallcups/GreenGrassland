package com.greengrassland.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建评论DTO
 */
@Data
public class CommentCreateDTO {

    @NotNull(message = "活动ID不能为空")
    private Long postId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 父评论ID（可选，用于回复评论）
     */
    private Long parentCommentId;
}
