package com.greengrassland.service.impl;

import com.greengrassland.entity.Post;
import com.greengrassland.entity.PostRegistration;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.repository.PostRegistrationRepository;
import com.greengrassland.repository.PostRepository;
import com.greengrassland.service.PostRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 活动报名服务实现
 */
@Service
@RequiredArgsConstructor
public class PostRegistrationServiceImpl implements PostRegistrationService {

    private final PostRepository postRepository;
    private final PostRegistrationRepository registrationRepository;

    @Override
    @Transactional
    public void registerPost(Long postId, Long userId) {
        // 检查活动是否存在
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            throw new BusinessException("活动不存在");
        }

        Post post = postOpt.get();

        // 检查是否已报名
        if (registrationRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new BusinessException("您已经报名了该活动");
        }

        // 检查报名人数是否已满
        long currentCount = registrationRepository.countByPostId(postId);
        if (currentCount >= post.getMaxPeople()) {
            throw new BusinessException("活动报名人数已满");
        }

        // 创建报名记录
        PostRegistration registration = PostRegistration.builder()
                .postId(postId)
                .userId(userId)
                .build();

        registrationRepository.save(registration);
    }

    @Override
    @Transactional
    public void cancelRegistration(Long postId, Long userId) {
        // 查找报名记录
        Optional<PostRegistration> registrationOpt = registrationRepository.findByPostIdAndUserId(postId, userId);
        if (registrationOpt.isEmpty()) {
            throw new BusinessException("您尚未报名该活动");
        }

        // 删除报名记录
        registrationRepository.delete(registrationOpt.get());
    }
}
