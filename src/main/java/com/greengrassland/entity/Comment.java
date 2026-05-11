package com.greengrassland.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论实体
 */
@Entity
@Table(name = "comment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 活动ID
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;

    /**
     * 评论用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 评论内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 父评论ID（用于嵌套回复，null表示顶级评论）
     */
    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
