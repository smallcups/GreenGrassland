package com.greengrassland.service;

import com.greengrassland.dto.UserDTO;

import java.util.List;

/**
 * 用户关注服务接口
 */
public interface UserFollowService {

    /**
     * 关注/取消关注
     */
    void toggleFollow(Long followerId, Long followingId);

    /**
     * 检查是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 获取粉丝列表
     */
    List<UserDTO> getFollowers(Long userId);

    /**
     * 获取关注列表
     */
    List<UserDTO> getFollowings(Long userId);

    /**
     * 获取粉丝数
     */
    long getFollowerCount(Long userId);

    /**
     * 获取关注数
     */
    long getFollowingCount(Long userId);
}
