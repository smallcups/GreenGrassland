package com.greengrassland.repository;

import com.greengrassland.entity.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户关注关系Repository
 */
@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    /**
     * 检查是否已关注
     */
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /**
     * 查找关注关系
     */
    Optional<UserFollow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    /**
     * 获取用户的粉丝列表（关注者ID）
     */
    @Query("SELECT uf.followerId FROM UserFollow uf WHERE uf.followingId = :userId")
    List<Long> findFollowerIdsByFollowingId(@Param("userId") Long userId);

    /**
     * 获取用户的关注列表（被关注者ID）
     */
    @Query("SELECT uf.followingId FROM UserFollow uf WHERE uf.followerId = :userId")
    List<Long> findFollowingIdsByFollowerId(@Param("userId") Long userId);

    /**
     * 统计粉丝数
     */
    long countByFollowingId(Long followingId);

    /**
     * 统计关注数
     */
    long countByFollowerId(Long followerId);

    /**
     * 查找用户关注的所有人
     */
    List<UserFollow> findByFollowerId(Long followerId);
}
