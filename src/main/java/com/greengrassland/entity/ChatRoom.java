package com.greengrassland.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 私聊房间实体
 */
@Entity
@Table(name = "chat_room", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user1_id", "user2_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户1ID（较小的ID）
     */
    @Column(name = "user1_id", nullable = false)
    private Long user1Id;

    /**
     * 用户2ID（较大的ID）
     */
    @Column(name = "user2_id", nullable = false)
    private Long user2Id;

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
