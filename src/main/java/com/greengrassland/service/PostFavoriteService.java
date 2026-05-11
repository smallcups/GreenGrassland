package com.greengrassland.service;

import com.greengrassland.dto.PostDTO;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface PostFavoriteService {

    /**
     * 收藏/取消收藏
     */
    void toggleFavorite(Long postId, Long userId);

    /**
     * 检查用户是否已收藏
     */
    boolean isFavorited(Long postId, Long userId);

    /**
     * 获取收藏数
     */
    long getFavoriteCount(Long postId);

    /**
     * 获取用户收藏的帖子列表
     */
    List<PostDTO> getUserFavorites(Long userId);
}
