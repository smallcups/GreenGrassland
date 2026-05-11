package com.greengrassland.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 点赞实体
 */
@Entity
@Table(name = "post_like", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 活动ID
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;

    /**
     * 点赞用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

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
