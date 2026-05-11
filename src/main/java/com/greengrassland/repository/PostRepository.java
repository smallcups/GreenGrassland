package com.greengrassland.repository;

import com.greengrassland.entity.Post;
import com.greengrassland.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 活动Repository
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 根据用户ID查找该用户发布的活动
     */
    List<Post> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 查找所有活动，按创建时间倒序
     */
    List<Post> findAllByOrderByCreateTimeDesc();

    /**
     * 根据活动类型查找活动
     */
    List<Post> findByTypeOrderByCreateTimeDesc(PostType type);

    /**
     * 模糊搜索帖子（标题或内容），支持类型筛选
     */
    @Query("SELECT p FROM Post p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:postType IS NULL OR p.type = :postType)")
    Page<Post> searchPosts(@Param("keyword") String keyword,
                           @Param("postType") PostType postType,
                           Pageable pageable);

    /**
     * 根据类型查找帖子，支持分页和排序
     */
    Page<Post> findByTypeOrderByCreateTimeDesc(PostType type, Pageable pageable);

    /**
     * 模糊搜索帖子（标题或内容），不限制类型
     */
    @Query("SELECT p FROM Post p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%'))")
    Page<Post> searchPostsByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
