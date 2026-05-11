package com.greengrassland.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动报名实体
 */
@Entity
@Table(name = "post_registration", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 活动ID
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;

    /**
     * 报名用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 报名时间
     */
    @Column(name = "register_time", nullable = false, updatable = false)
    private LocalDateTime registerTime;

    @PrePersist
    protected void onCreate() {
        registerTime = LocalDateTime.now();
    }
}
