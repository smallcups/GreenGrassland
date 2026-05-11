package com.greengrassland.repository;

import com.greengrassland.entity.PostRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 活动报名Repository
 */
@Repository
public interface PostRegistrationRepository extends JpaRepository<PostRegistration, Long> {

    /**
     * 检查用户是否已报名该活动
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * 根据活动ID和用户ID查找报名记录
     */
    Optional<PostRegistration> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * 统计活动的报名人数
     */
    long countByPostId(Long postId);

    /**
     * 批量统计活动的报名人数
     */
    @org.springframework.data.jpa.repository.Query("SELECT pr.postId, COUNT(pr) FROM PostRegistration pr WHERE pr.postId IN :postIds GROUP BY pr.postId")
    List<Object[]> countByPostIdIn(@org.springframework.data.repository.query.Param("postIds") List<Long> postIds);

    /**
     * 根据用户ID查找该用户报名的所有活动ID
     */
    @Query("SELECT pr.postId FROM PostRegistration pr WHERE pr.userId = :userId")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查找该用户的所有报名记录
     */
    List<PostRegistration> findByUserIdOrderByRegisterTimeDesc(Long userId);

    /**
     * 根据活动ID查找该活动的所有报名记录
     */
    List<PostRegistration> findByPostId(Long postId);
}
