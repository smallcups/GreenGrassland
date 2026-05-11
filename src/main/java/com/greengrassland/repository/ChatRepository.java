package com.greengrassland.repository;

import com.greengrassland.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 私聊消息Repository
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    /**
     * 根据房间ID查找消息，按时间正序
     */
    List<Chat> findByRoomIdOrderByCreateTimeAsc(Long roomId);

    /**
     * 查找房间的最后一条消息
     */
    Chat findFirstByRoomIdOrderByCreateTimeDesc(Long roomId);

    /**
     * 统计未读消息数
     */
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    /**
     * 统计房间的未读消息数
     */
    long countByRoomIdAndReceiverIdAndIsReadFalse(Long roomId, Long receiverId);

    /**
     * 标记房间消息为已读
     */
    @Modifying
    @Transactional
    @Query("UPDATE Chat c SET c.isRead = true WHERE c.roomId = :roomId AND c.receiverId = :receiverId")
    void markAsReadByRoomIdAndReceiverId(@Param("roomId") Long roomId, @Param("receiverId") Long receiverId);
}
