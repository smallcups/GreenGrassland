package com.greengrassland.repository;

import com.greengrassland.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评论Repository
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 根据活动ID查找所有评论，按创建时间正序
     */
    List<Comment> findByPostIdOrderByCreateTimeAsc(Long postId);

    /**
     * 根据父评论ID查找所有子评论，按创建时间正序
     */
    List<Comment> findByParentCommentIdOrderByCreateTimeAsc(Long parentCommentId);

    /**
     * 统计活动的评论数（包括所有层级的评论）
     */
    long countByPostId(Long postId);

    /**
     * 批量统计活动的评论数
     */
    @org.springframework.data.jpa.repository.Query("SELECT c.postId, COUNT(c) FROM Comment c WHERE c.postId IN :postIds GROUP BY c.postId")
    List<Object[]> countByPostIdIn(@org.springframework.data.repository.query.Param("postIds") java.util.List<Long> postIds);
}
