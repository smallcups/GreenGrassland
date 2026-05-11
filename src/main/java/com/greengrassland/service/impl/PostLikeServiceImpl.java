package com.greengrassland.service.impl;

import com.greengrassland.entity.Post;
import com.greengrassland.entity.PostLike;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.repository.PostLikeRepository;
import com.greengrassland.repository.PostRepository;
import com.greengrassland.service.NotificationService;
import com.greengrassland.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 点赞服务实现
 */
@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository likeRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void toggleLike(Long postId, Long userId) {
        // 检查活动是否存在
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            throw new BusinessException("活动不存在");
        }

        Post post = postOpt.get();
        
        // 查找是否已点赞
        Optional<PostLike> likeOpt = likeRepository.findByPostIdAndUserId(postId, userId);
        if (likeOpt.isPresent()) {
            // 已点赞，取消点赞
            likeRepository.delete(likeOpt.get());
        } else {
            // 未点赞，添加点赞
            PostLike like = PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            likeRepository.save(like);
            
            // 发送通知给帖子作者（如果点赞者不是作者）
            if (!post.getUserId().equals(userId)) {
                notificationService.createNotification(
                    "LIKE",
                    post.getUserId(),
                    postId,
                    userId,
                    "点赞了你的活动"
                );
            }
        }
    }

    @Override
    public boolean isLiked(Long postId, Long userId) {
        if (userId == null) {
            return false;
        }
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    public long getLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }
}
