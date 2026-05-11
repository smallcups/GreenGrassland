package com.greengrassland.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动帖子实体
 */
@Entity
@Table(name = "post")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发布者ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 活动类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PostType type;

    /**
     * 最大人数
     */
    @Column(name = "max_people", nullable = false)
    private Integer maxPeople;

    /**
     * 活动时间
     */
    @Column(name = "activity_time")
    private LocalDateTime activityTime;

    /**
     * 活动地点
     */
    @Column(nullable = false, length = 200)
    private String location;

    /**
     * 图片URL（多个图片用逗号分隔）
     */
    @Column(name = "images", length = 2000)
    private String images;

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
