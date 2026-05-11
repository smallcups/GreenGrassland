package com.greengrassland.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 群聊房间实体
 */
@Entity
@Table(name = "group_chat_room")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 活动ID（群聊基于活动创建）
     */
    @Column(name = "post_id", nullable = false, unique = true)
    private Long postId;

    /**
     * 创建者ID
     */
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 最后消息时间
     */
    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        lastMessageTime = LocalDateTime.now();
    }
}
