package com.greengrassland.service;

import com.greengrassland.dto.UserDTO;
import com.greengrassland.dto.UserLoginDTO;
import com.greengrassland.dto.UserRegisterDTO;
import com.greengrassland.dto.UserUpdateDTO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    UserDTO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     */
    UserDTO login(UserLoginDTO loginDTO);

    /**
     * 获取当前用户信息
     */
    UserDTO getCurrentUser(Long userId);

    /**
     * 根据ID获取用户信息
     */
    UserDTO getUserById(Long userId);

    /**
     * 更新用户头像
     */
    void updateAvatar(Long userId, String avatarUrl);

    /**
     * 更新用户资料
     */
    UserDTO updateProfile(Long userId, UserUpdateDTO updateDTO);
}
