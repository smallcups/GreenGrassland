package com.greengrassland.service.impl;

import com.greengrassland.dto.UserDTO;
import com.greengrassland.dto.UserLoginDTO;
import com.greengrassland.dto.UserRegisterDTO;
import com.greengrassland.dto.UserUpdateDTO;
import com.greengrassland.entity.User;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.repository.UserRepository;
import com.greengrassland.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public UserDTO register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = User.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .nickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername())
                .email(registerDTO.getEmail())
                .build();

        user = userRepository.save(user);

        return convertToDTO(user);
    }

    @Override
    public UserDTO login(UserLoginDTO loginDTO) {
        // 查找用户
        Optional<User> userOpt = userRepository.findByUsername(loginDTO.getUsername());
        if (userOpt.isEmpty()) {
            throw new BusinessException("用户名或密码错误");
        }

        User user = userOpt.get();

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        return convertToDTO(user);
    }

    @Override
    public UserDTO getCurrentUser(Long userId) {
        return getUserById(userId);
    }

    @Override
    public UserDTO getUserById(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new BusinessException("用户不存在");
        }

        return convertToDTO(userOpt.get());
    }

    @Override
    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new BusinessException("用户不存在");
        }

        User user = userOpt.get();
        user.setAvatar(avatarUrl);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDTO updateProfile(Long userId, UserUpdateDTO updateDTO) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new BusinessException("用户不存在");
        }

        User user = userOpt.get();
        if (updateDTO.getNickname() != null && !updateDTO.getNickname().trim().isEmpty()) {
            user.setNickname(updateDTO.getNickname().trim());
        }
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().trim().isEmpty()) {
            user.setEmail(updateDTO.getEmail().trim());
        }

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    /**
     * 转换为DTO
     */
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .createTime(user.getCreateTime())
                .build();
    }
}
