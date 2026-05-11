package com.greengrassland.service;

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
}
