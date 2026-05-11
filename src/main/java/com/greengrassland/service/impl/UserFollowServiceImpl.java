package com.greengrassland.service.impl;

import com.greengrassland.dto.UserDTO;
import com.greengrassland.entity.User;
import com.greengrassland.entity.UserFollow;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.repository.UserFollowRepository;
import com.greengrassland.repository.UserRepository;
import com.greengrassland.service.NotificationService;
import com.greengrassland.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户关注服务实现
 */
@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl implements UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void toggleFollow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException("不能关注自己");
        }

        Optional<UserFollow> followOpt = userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId);
        if (followOpt.isPresent()) {
            // 已关注，取消关注
            userFollowRepository.delete(followOpt.get());
        } else {
            // 未关注，添加关注
            UserFollow follow = UserFollow.builder()
                    .followerId(followerId)
                    .followingId(followingId)
                    .build();
            userFollowRepository.save(follow);
            
            // 发送关注通知
            notificationService.createNotification(
                "FOLLOW",
                followingId,
                null,
                followerId,
                "关注了你"
            );
        }
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        return userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public List<UserDTO> getFollowers(Long userId) {
        List<Long> followerIds = userFollowRepository.findFollowerIdsByFollowingId(userId);
        List<User> users = userRepository.findAllById(followerIds);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getFollowings(Long userId) {
        List<Long> followingIds = userFollowRepository.findFollowingIdsByFollowerId(userId);
        List<User> users = userRepository.findAllById(followingIds);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long getFollowerCount(Long userId) {
        return userFollowRepository.countByFollowingId(userId);
    }

    @Override
    public long getFollowingCount(Long userId) {
        return userFollowRepository.countByFollowerId(userId);
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
