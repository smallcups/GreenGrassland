package com.greengrassland.repository;

import com.greengrassland.entity.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 群聊消息Repository
 */
@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {

    /**
     * 根据房间ID查找消息，按时间正序
     */
    List<GroupChat> findByRoomIdOrderByCreateTimeAsc(Long roomId);

    /**
     * 查找房间的最后一条消息
     */
    GroupChat findFirstByRoomIdOrderByCreateTimeDesc(Long roomId);
}
