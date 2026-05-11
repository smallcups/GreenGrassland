package com.greengrassland.repository;

import com.greengrassland.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 聊天房间Repository
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 查找两个用户之间的聊天房间
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE (cr.user1Id = :user1Id AND cr.user2Id = :user2Id) OR (cr.user1Id = :user2Id AND cr.user2Id = :user1Id)")
    Optional<ChatRoom> findByUser1IdAndUser2Id(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    /**
     * 查找用户参与的所有聊天房间
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.user1Id = :userId OR cr.user2Id = :userId ORDER BY cr.lastMessageTime DESC")
    List<ChatRoom> findByUserId(@Param("userId") Long userId);
}
