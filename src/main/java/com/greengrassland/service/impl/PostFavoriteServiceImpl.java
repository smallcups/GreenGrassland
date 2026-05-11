package com.greengrassland.service.impl;

import com.greengrassland.dto.PostDTO;
import com.greengrassland.entity.Post;
import com.greengrassland.entity.PostFavorite;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.repository.PostFavoriteRepository;
import com.greengrassland.repository.PostRepository;
import com.greengrassland.service.PostFavoriteService;
import com.greengrassland.service.PostService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 收藏服务实现
 */
@Service
public class PostFavoriteServiceImpl implements PostFavoriteService {

    private final PostFavoriteRepository favoriteRepository;
    private final PostRepository postRepository;
    private final PostService postService;

    public PostFavoriteServiceImpl(PostFavoriteRepository favoriteRepository,
                                    PostRepository postRepository,
                                    @Lazy PostService postService) {
        this.favoriteRepository = favoriteRepository;
        this.postRepository = postRepository;
        this.postService = postService;
    }

    @Override
    @Transactional
    public void toggleFavorite(Long postId, Long userId) {
        // 检查活动是否存在
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            throw new BusinessException("活动不存在");
        }

        // 查找是否已收藏
        Optional<PostFavorite> favoriteOpt = favoriteRepository.findByPostIdAndUserId(postId, userId);
        if (favoriteOpt.isPresent()) {
            // 已收藏，取消收藏
            favoriteRepository.delete(favoriteOpt.get());
        } else {
            // 未收藏，添加收藏
            PostFavorite favorite = PostFavorite.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            favoriteRepository.save(favorite);
        }
    }

    @Override
    public boolean isFavorited(Long postId, Long userId) {
        if (userId == null) {
            return false;
        }
        return favoriteRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    public long getFavoriteCount(Long postId) {
        return favoriteRepository.countByPostId(postId);
    }

    @Override
    public List<PostDTO> getUserFavorites(Long userId) {
        // 获取用户收藏的帖子ID列表
        List<Long> postIds = favoriteRepository.findPostIdsByUserId(userId);
        if (postIds.isEmpty()) {
            return List.of();
        }

        // 根据ID列表查找帖子
        List<Post> posts = postRepository.findAllById(postIds);
        
        // 转换为DTO（使用PostService的方法）
        return posts.stream()
                .map(post -> postService.getPostDetail(post.getId(), userId))
                .collect(Collectors.toList());
    }
}
