package com.greengrassland.repository;

import com.greengrassland.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 点赞Repository
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 检查用户是否已点赞该活动
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * 根据活动ID和用户ID查找点赞记录
     */
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * 统计活动的点赞数
     */
    long countByPostId(Long postId);

    /**
     * 批量统计活动的点赞数
     */
    @org.springframework.data.jpa.repository.Query("SELECT pl.postId, COUNT(pl) FROM PostLike pl WHERE pl.postId IN :postIds GROUP BY pl.postId")
    List<Object[]> countByPostIdIn(@org.springframework.data.repository.query.Param("postIds") List<Long> postIds);

    /**
     * 查找用户在指定帖子列表中点赞的帖子ID
     */
    @org.springframework.data.jpa.repository.Query("SELECT pl.postId FROM PostLike pl WHERE pl.userId = :userId AND pl.postId IN :postIds")
    List<Long> findLikedPostIds(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("postIds") List<Long> postIds);

    /**
     * 统计用户发布的活动收到的总点赞数
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(pl) FROM PostLike pl JOIN Post p ON pl.postId = p.id WHERE p.userId = :userId")
    long countByPostUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
