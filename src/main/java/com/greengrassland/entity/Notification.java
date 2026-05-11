package com.greengrassland.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Entity
@Table(name = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 接收通知的用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 通知类型：LIKE（点赞）、COMMENT（评论）、FOLLOW（关注）、POST（新活动）
     */
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    /**
     * 关联的活动ID（可选）
     */
    @Column(name = "post_id")
    private Long postId;

    /**
     * 关联的用户ID（触发通知的用户，可选）
     */
    @Column(name = "from_user_id")
    private Long fromUserId;

    /**
     * 通知内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 是否已读
     */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }
}
