package com.greengrassland.repository;

import com.greengrassland.entity.GroupChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 群聊房间Repository
 */
@Repository
public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {

    /**
     * 根据活动ID查找群聊房间
     */
    Optional<GroupChatRoom> findByPostId(Long postId);
}
