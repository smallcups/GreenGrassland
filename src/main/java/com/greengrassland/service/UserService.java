package com.greengrassland.service;

import com.greengrassland.dto.UserDTO;
import com.greengrassland.dto.UserLoginDTO;
import com.greengrassland.dto.UserPasswordDTO;
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

    /**
     * 修改密码
     */
    void updatePassword(Long userId, UserPasswordDTO passwordDTO);

    /**
     * 重置密码（通过用户名+邮箱验证）
     */
    void resetPassword(String username, String email, String newPassword);
}
