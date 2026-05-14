package com.greengrassland.service.impl;

import com.greengrassland.dto.UserDTO;
import com.greengrassland.dto.UserLoginDTO;
import com.greengrassland.dto.UserPasswordDTO;
import com.greengrassland.dto.UserRegisterDTO;
import com.greengrassland.dto.UserUpdateDTO;
import com.greengrassland.entity.User;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.repository.PostFavoriteRepository;
import com.greengrassland.repository.PostLikeRepository;
import com.greengrassland.repository.PostRepository;
import com.greengrassland.repository.UserFollowRepository;
import com.greengrassland.repository.UserRepository;
import com.greengrassland.service.SensitiveWordFilter;
import com.greengrassland.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostFavoriteRepository postFavoriteRepository;
    private final UserFollowRepository userFollowRepository;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public UserDTO register(UserRegisterDTO registerDTO) {
        // 敏感词检查
        String matched = sensitiveWordFilter.findFirstMatch(registerDTO.getUsername());
        if (matched != null) throw new BusinessException("用户名包含敏感词");
        if (registerDTO.getNickname() != null) {
            matched = sensitiveWordFilter.findFirstMatch(registerDTO.getNickname());
            if (matched != null) throw new BusinessException("昵称包含敏感词");
        }

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
    @Cacheable(value = "user", key = "#userId", unless = "#result == null")
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
    @CacheEvict(value = "user", key = "#userId")
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
        if (updateDTO.getBio() != null) {
            user.setBio(updateDTO.getBio().trim());
        }
        if (updateDTO.getInterestTags() != null) {
            user.setInterestTags(updateDTO.getInterestTags().trim());
        }

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, UserPasswordDTO passwordDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("当前密码不正确");
        }

        if (passwordDTO.getNewPassword().length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(String username, String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) throw new BusinessException("用户名不存在");
        User user = userOpt.get();
        if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(email.trim()))
            throw new BusinessException("邮箱不匹配");
        if (newPassword == null || newPassword.length() < 6)
            throw new BusinessException("新密码至少6位");
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        Long userId = user.getId();
        return UserDTO.builder()
                .id(userId)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .interestTags(user.getInterestTags())
                .postCount(postRepository.countByUserId(userId))
                .likeCount(postLikeRepository.countByPostUserId(userId))
                .favoriteCount(postFavoriteRepository.countByPostUserId(userId))
                .followerCount(userFollowRepository.countByFollowingId(userId))
                .followingCount(userFollowRepository.countByFollowerId(userId))
                .createTime(user.getCreateTime())
                .build();
    }
}
