package com.greengrassland.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体
 */
@Entity
@Table(name = "user_follow",
       uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关注者ID（粉丝）
     */
    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    /**
     * 被关注者ID（关注的人）
     */
    @Column(name = "following_id", nullable = false)
    private Long followingId;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
