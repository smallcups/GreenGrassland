package com.greengrassland.service;

import com.greengrassland.dto.UserDTO;

import java.util.List;

/**
 * 活动报名服务接口
 */
public interface PostRegistrationService {

    /**
     * 报名活动
     */
    void registerPost(Long postId, Long userId);

    /**
     * 取消报名
     */
    void cancelRegistration(Long postId, Long userId);

    /**
     * 获取活动参与者列表
     */
    List<UserDTO> getParticipants(Long postId);
}
