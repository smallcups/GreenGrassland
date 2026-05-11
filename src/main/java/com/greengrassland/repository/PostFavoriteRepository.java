package com.greengrassland.repository;

import com.greengrassland.entity.PostFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 收藏Repository
 */
@Repository
public interface PostFavoriteRepository extends JpaRepository<PostFavorite, Long> {

    /**
     * 检查用户是否已收藏该活动
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * 根据活动ID和用户ID查找收藏记录
     */
    Optional<PostFavorite> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * 统计活动的收藏数
     */
    long countByPostId(Long postId);

    /**
     * 批量统计活动的收藏数
     */
    @org.springframework.data.jpa.repository.Query("SELECT pf.postId, COUNT(pf) FROM PostFavorite pf WHERE pf.postId IN :postIds GROUP BY pf.postId")
    List<Object[]> countByPostIdIn(@org.springframework.data.repository.query.Param("postIds") java.util.List<Long> postIds);

    /**
     * 查找用户在指定帖子列表中收藏的帖子ID
     */
    @org.springframework.data.jpa.repository.Query("SELECT pf.postId FROM PostFavorite pf WHERE pf.userId = :userId AND pf.postId IN :postIds")
    java.util.List<Long> findFavoritedPostIds(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("postIds") java.util.List<Long> postIds);

    /**
     * 根据用户ID查找该用户收藏的所有帖子ID
     */
    @Query("SELECT pf.postId FROM PostFavorite pf WHERE pf.userId = :userId ORDER BY pf.createTime DESC")
    java.util.List<Long> findPostIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查找该用户的所有收藏记录
     */
    java.util.List<PostFavorite> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 统计用户发布的活动收到的总收藏数
     */
    @Query("SELECT COUNT(pf) FROM PostFavorite pf JOIN Post p ON pf.postId = p.id WHERE p.userId = :userId")
    long countByPostUserId(@Param("userId") Long userId);
}
