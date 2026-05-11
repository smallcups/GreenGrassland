package com.greengrassland.repository;

import com.greengrassland.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 根据用户ID查找所有通知，按创建时间倒序
     */
    List<Notification> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 统计用户未读通知数
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 将用户的所有通知标记为已读
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);
}
