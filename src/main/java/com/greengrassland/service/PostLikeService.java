package com.greengrassland.service;

/**
 * 点赞服务接口
 */
public interface PostLikeService {

    /**
     * 点赞/取消点赞
     */
    void toggleLike(Long postId, Long userId);

    /**
     * 检查用户是否已点赞
     */
    boolean isLiked(Long postId, Long userId);

    /**
     * 获取活动的点赞数
     */
    long getLikeCount(Long postId);
}
