package com.greengrassland.service.impl;

import com.greengrassland.dto.PostCreateDTO;
import com.greengrassland.dto.PostDTO;
import com.greengrassland.dto.PostSearchDTO;
import com.greengrassland.entity.Post;
import com.greengrassland.entity.PostType;
import com.greengrassland.entity.User;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.repository.CommentRepository;
import com.greengrassland.repository.PostFavoriteRepository;
import com.greengrassland.repository.PostLikeRepository;
import com.greengrassland.repository.PostRegistrationRepository;
import com.greengrassland.repository.PostRepository;
import com.greengrassland.repository.UserRepository;
import com.greengrassland.service.CommentService;
import com.greengrassland.service.PostFavoriteService;
import com.greengrassland.service.PostLikeService;
import com.greengrassland.service.PostService;
import com.greengrassland.service.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greengrassland.dto.PageDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostRegistrationRepository registrationRepository;
    private final PostLikeRepository likeRepository;
    private final PostFavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostLikeService likeService;
    private final PostFavoriteService favoriteService;
    private final CommentService commentService;
    private final SensitiveWordFilter sensitiveWordFilter;

    @Override
    @Transactional
    public PostDTO createPost(Long userId, PostCreateDTO createDTO) {
        String matched = sensitiveWordFilter.findFirstMatch(createDTO.getTitle());
        if (matched != null) throw new BusinessException("标题包含敏感词");
        if (createDTO.getContent() != null) {
            matched = sensitiveWordFilter.findFirstMatch(createDTO.getContent());
            if (matched != null) throw new BusinessException("内容包含敏感词");
        }

        Post post = Post.builder()
                .userId(userId)
                .title(createDTO.getTitle())
                .content(createDTO.getContent())
                .type(createDTO.getType())
                .maxPeople(createDTO.getMaxPeople())
                .activityTime(createDTO.getActivityTime())
                .location(createDTO.getLocation())
                .images(createDTO.getImages())
                .build();

        post = postRepository.save(post);

        return convertToDTO(post, userId, false, false);
    }

    @Override
    public List<PostDTO> getPostList(Long currentUserId) {
        List<Post> posts = postRepository.findAllByOrderByCreateTimeDesc();
        return batchConvertToDTOs(posts, currentUserId, false);
    }

    @Override
    public PageDTO<PostDTO> searchPosts(PostSearchDTO searchDTO, Long currentUserId) {
        String sortBy = searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "createTime";
        String sortOrder = searchDTO.getSortOrder() != null && searchDTO.getSortOrder().equalsIgnoreCase("ASC") ? "ASC" : "DESC";

        Sort sort = Sort.by(sortOrder.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(
                (searchDTO.getPage() != null && searchDTO.getPage() > 0) ? searchDTO.getPage() - 1 : 0,
                (searchDTO.getPageSize() != null && searchDTO.getPageSize() > 0) ? searchDTO.getPageSize() : 20,
                sort
        );

        String keyword = (searchDTO.getKeyword() != null && !searchDTO.getKeyword().trim().isEmpty())
                ? searchDTO.getKeyword().trim() : null;
        String location = (searchDTO.getLocation() != null && !searchDTO.getLocation().trim().isEmpty())
                ? searchDTO.getLocation().trim() : null;
        PostType postType = null;
        String typeStr = searchDTO.getType();

        if (typeStr != null && !typeStr.trim().isEmpty()) {
            try {
                postType = PostType.valueOf(typeStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // 忽略无效类型
            }
        }

        Page<Post> postPage;
        boolean hasFilter = keyword != null || location != null || postType != null;
        if (hasFilter) {
            postPage = postRepository.searchPostsWithLocation(keyword, location, postType, pageable);
        } else {
            postPage = postRepository.findAll(pageable);
        }

        List<PostDTO> dtos = batchConvertToDTOs(postPage.getContent(), currentUserId, false);
        return PageDTO.<PostDTO>builder()
                .content(dtos)
                .page(postPage.getNumber() + 1)
                .pageSize(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .hasNext(postPage.hasNext())
                .hasPrevious(postPage.hasPrevious())
                .build();
    }

    @Override
    public PostDTO getPostDetail(Long postId, Long currentUserId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            throw new BusinessException("活动不存在");
        }

        Post post = postOpt.get();
        boolean isRegistered = currentUserId != null
                && registrationRepository.existsByPostIdAndUserId(postId, currentUserId);

        return convertToDTO(post, currentUserId, isRegistered, true);
    }

    @Override
    public List<PostDTO> getMyPosts(Long userId) {
        List<Post> posts = postRepository.findByUserIdOrderByCreateTimeDesc(userId);
        return batchConvertToDTOs(posts, userId, false);
    }

    @Override
    public List<PostDTO> getMyRegistrations(Long userId) {
        List<Long> postIds = registrationRepository.findPostIdsByUserId(userId);
        if (postIds.isEmpty()) {
            return List.of();
        }

        List<Post> posts = postRepository.findAllById(postIds);
        return batchConvertToDTOs(posts, userId, true);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
            throw new BusinessException("活动不存在");
        }

        Post post = postOpt.get();
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己发布的活动");
        }

        registrationRepository.findByPostId(postId).forEach(registrationRepository::delete);
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public void cancelPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("只能取消自己发布的活动");
        }
        post.setStatus(com.greengrassland.entity.PostStatus.CANCELLED);
        postRepository.save(post);
    }

    /**
     * 批量转换为DTO，使用批量查询避免N+1问题
     */
    private List<PostDTO> batchConvertToDTOs(List<Post> posts, Long currentUserId, boolean allRegistered) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        // 批量加载用户信息
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量加载报名数
        Map<Long, Long> registrationCountMap = new HashMap<>();
        if (!postIds.isEmpty()) {
            List<Object[]> regCounts = registrationRepository.countByPostIdIn(postIds);
            for (Object[] row : regCounts) {
                registrationCountMap.put((Long) row[0], (Long) row[1]);
            }
        }

        // 批量加载当前用户的报名状态
        Set<Long> registeredPostIds = currentUserId != null
                ? registrationRepository.findPostIdsByUserId(currentUserId).stream().collect(Collectors.toSet())
                : Set.of();

        // 批量加载点赞数
        Map<Long, Long> likeCountMap = new HashMap<>();
        if (!postIds.isEmpty()) {
            List<Object[]> likeCounts = likeRepository.countByPostIdIn(postIds);
            for (Object[] row : likeCounts) {
                likeCountMap.put((Long) row[0], (Long) row[1]);
            }
        }

        // 批量加载当前用户的点赞状态
        Set<Long> likedPostIds = currentUserId != null
                ? new java.util.HashSet<>(likeRepository.findLikedPostIds(currentUserId, postIds))
                : Set.of();

        // 批量加载收藏数
        Map<Long, Long> favoriteCountMap = new HashMap<>();
        if (!postIds.isEmpty()) {
            List<Object[]> favCounts = favoriteRepository.countByPostIdIn(postIds);
            for (Object[] row : favCounts) {
                favoriteCountMap.put((Long) row[0], (Long) row[1]);
            }
        }

        // 批量加载当前用户的收藏状态
        Set<Long> favoritedPostIds = currentUserId != null
                ? new java.util.HashSet<>(favoriteRepository.findFavoritedPostIds(currentUserId, postIds))
                : Set.of();

        // 批量加载评论数
        Map<Long, Long> commentCountMap = new HashMap<>();
        if (!postIds.isEmpty()) {
            List<Object[]> commentCounts = commentRepository.countByPostIdIn(postIds);
            for (Object[] row : commentCounts) {
                commentCountMap.put((Long) row[0], (Long) row[1]);
            }
        }

        return posts.stream()
                .map(post -> {
                    boolean isRegistered = allRegistered || (currentUserId != null && registeredPostIds.contains(post.getId()));
                    return convertToDTOWithMaps(post, currentUserId, isRegistered, false,
                            userMap, registrationCountMap, likeCountMap, likedPostIds, favoriteCountMap, favoritedPostIds, commentCountMap);
                })
                .collect(Collectors.toList());
    }

    private PostDTO convertToDTO(Post post, Long currentUserId, boolean isRegistered, boolean includeComments) {
        Optional<User> userOpt = userRepository.findById(post.getUserId());
        String username = userOpt.map(User::getUsername).orElse("未知用户");
        String nickname = userOpt.map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                .orElse("未知用户");
        String userAvatar = userOpt.map(u -> u.getAvatar() != null ? u.getAvatar() : "/images/default-avatar.svg")
                .orElse("/images/default-avatar.svg");

        long currentPeople = registrationRepository.countByPostId(post.getId());

        long likeCount = likeService.getLikeCount(post.getId());
        boolean isLiked = likeService.isLiked(post.getId(), currentUserId);

        long favoriteCount = favoriteService.getFavoriteCount(post.getId());
        boolean isFavorited = favoriteService.isFavorited(post.getId(), currentUserId);

        long commentCount = includeComments ? commentService.getCommentsByPostId(post.getId()).size() : 0;

        PostDTO.PostDTOBuilder builder = PostDTO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .username(username)
                .nickname(nickname)
                .userAvatar(userAvatar)
                .title(post.getTitle())
                .content(post.getContent())
                .type(post.getType())
                .maxPeople(post.getMaxPeople())
                .currentPeople((int) currentPeople)
                .activityTime(post.getActivityTime())
                .location(post.getLocation())
                .images(post.getImages())
                .status(post.getStatus())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .isRegistered(isRegistered)
                .isLiked(isLiked)
                .isFavorited(isFavorited)
                .likeCount((int) likeCount)
                .favoriteCount((int) favoriteCount)
                .commentCount((int) commentCount);

        if (includeComments) {
            builder.comments(commentService.getCommentsByPostId(post.getId()));
        } else {
            builder.comments(new ArrayList<>());
        }

        return builder.build();
    }

    private PostDTO convertToDTOWithMaps(Post post, Long currentUserId, boolean isRegistered, boolean includeComments,
                                          Map<Long, User> userMap, Map<Long, Long> registrationCountMap,
                                          Map<Long, Long> likeCountMap, Set<Long> likedPostIds,
                                          Map<Long, Long> favoriteCountMap, Set<Long> favoritedPostIds,
                                          Map<Long, Long> commentCountMap) {
        User user = userMap.get(post.getUserId());
        String username = user != null ? user.getUsername() : "未知用户";
        String nickname = user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "未知用户";
        String userAvatar = user != null ? (user.getAvatar() != null ? user.getAvatar() : "/images/default-avatar.svg") : "/images/default-avatar.svg";

        long currentPeople = registrationCountMap.getOrDefault(post.getId(), 0L);

        long likeCount = likeCountMap.getOrDefault(post.getId(), 0L);
        boolean isLiked = currentUserId != null && likedPostIds.contains(post.getId());

        long favoriteCount = favoriteCountMap.getOrDefault(post.getId(), 0L);
        boolean isFavorited = currentUserId != null && favoritedPostIds.contains(post.getId());

        long commentCount = commentCountMap.getOrDefault(post.getId(), 0L);

        PostDTO.PostDTOBuilder builder = PostDTO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .username(username)
                .nickname(nickname)
                .userAvatar(userAvatar)
                .title(post.getTitle())
                .content(post.getContent())
                .type(post.getType())
                .maxPeople(post.getMaxPeople())
                .currentPeople((int) currentPeople)
                .activityTime(post.getActivityTime())
                .location(post.getLocation())
                .images(post.getImages())
                .status(post.getStatus())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .isRegistered(isRegistered)
                .isLiked(isLiked)
                .isFavorited(isFavorited)
                .likeCount((int) likeCount)
                .favoriteCount((int) favoriteCount)
                .commentCount((int) commentCount);

        if (includeComments) {
            builder.comments(commentService.getCommentsByPostId(post.getId()));
        } else {
            builder.comments(new ArrayList<>());
        }

        return builder.build();
    }
}
