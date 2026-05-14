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
@Table(name = "post", indexes = {
    @Index(name = "idx_post_status", columnList = "status"),
    @Index(name = "idx_post_activity_time", columnList = "activity_time"),
    @Index(name = "idx_post_lat_lng", columnList = "latitude,longitude")
})
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

    @Column(columnDefinition = "DECIMAL(10,7)")
    private Double latitude;

    @Column(columnDefinition = "DECIMAL(10,7)")
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.RECRUITING;

    /**
     * 图片URL（多个图片用逗号分隔）
     */
    @Column(name = "images", length = 2000)
    private String images;

    @Column(name = "approval_mode")
    @Builder.Default
    private Boolean approvalMode = false;

    @Column(name = "is_series")
    @Builder.Default
    private Boolean isSeries = false;

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
