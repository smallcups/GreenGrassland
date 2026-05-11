package com.greengrassland.service.impl;

import com.greengrassland.dto.CommentCreateDTO;
import com.greengrassland.dto.CommentDTO;
import com.greengrassland.entity.Comment;
import com.greengrassland.entity.Post;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.repository.CommentRepository;
import com.greengrassland.repository.PostRepository;
import com.greengrassland.repository.UserRepository;
import com.greengrassland.service.CommentService;
import com.greengrassland.service.NotificationService;
import com.greengrassland.service.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SensitiveWordFilter sensitiveWordFilter;

    @Override
    @Transactional
    public CommentDTO createComment(Long userId, CommentCreateDTO createDTO) {
        String matched = sensitiveWordFilter.findFirstMatch(createDTO.getContent());
        if (matched != null) throw new BusinessException("评论包含敏感词");

        // 检查活动是否存在
        Optional<Post> postOpt = postRepository.findById(createDTO.getPostId());
        if (postOpt.isEmpty()) {
            throw new BusinessException("活动不存在");
        }

        // 如果指定了父评论ID，验证父评论是否存在且属于同一活动
        if (createDTO.getParentCommentId() != null) {
            Optional<Comment> parentCommentOpt = commentRepository.findById(createDTO.getParentCommentId());
            if (parentCommentOpt.isEmpty()) {
                throw new BusinessException("父评论不存在");
            }
            Comment parentComment = parentCommentOpt.get();
            if (!parentComment.getPostId().equals(createDTO.getPostId())) {
                throw new BusinessException("父评论不属于该活动");
            }
        }

        // 创建评论
        Comment comment = Comment.builder()
                .postId(createDTO.getPostId())
                .userId(userId)
                .content(createDTO.getContent())
                .parentCommentId(createDTO.getParentCommentId())
                .build();

        comment = commentRepository.save(comment);

        // 发送通知给帖子作者（如果评论者不是作者）
        Post post = postOpt.get();
        if (!post.getUserId().equals(userId)) {
            notificationService.createNotification(
                "COMMENT",
                post.getUserId(),
                createDTO.getPostId(),
                userId,
                "评论了你的活动"
            );
        }

        return convertToDTO(comment, true);
    }

    @Override
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        // 获取所有评论（包括顶级和回复）
        List<Comment> allComments = commentRepository.findByPostIdOrderByCreateTimeAsc(postId);
        
        // 构建评论树
        return buildCommentTree(allComments);
    }

    /**
     * 构建评论树（将平铺的评论列表转换为树形结构）
     */
    private List<CommentDTO> buildCommentTree(List<Comment> allComments) {
        if (allComments.isEmpty()) {
            return List.of();
        }

        // 批量加载所有相关用户
        List<Long> userIds = allComments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        java.util.Map<Long, com.greengrassland.entity.User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(com.greengrassland.entity.User::getId, u -> u));

        // 创建评论ID到Comment实体的映射
        java.util.Map<Long, Comment> commentEntityMap = allComments.stream()
                .collect(Collectors.toMap(Comment::getId, comment -> comment));

        // 将所有评论转换为DTO
        List<CommentDTO> commentDTOs = new java.util.ArrayList<>();
        for (Comment comment : allComments) {
            CommentDTO dto = convertToDTOWithUserMap(comment, userMap);

            if (comment.getParentCommentId() != null) {
                Comment parentComment = commentEntityMap.get(comment.getParentCommentId());
                if (parentComment != null) {
                    com.greengrassland.entity.User parentUser = userMap.get(parentComment.getUserId());
                    dto.setParentCommentUsername(parentUser != null ? parentUser.getUsername() : null);
                }
            }

            commentDTOs.add(dto);
        }

        // 创建评论ID到DTO的映射
        java.util.Map<Long, CommentDTO> commentMap = commentDTOs.stream()
                .collect(Collectors.toMap(CommentDTO::getId, dto -> dto));

        // 构建树形结构
        List<CommentDTO> rootComments = new java.util.ArrayList<>();
        for (CommentDTO dto : commentDTOs) {
            if (dto.getParentCommentId() == null) {
                rootComments.add(dto);
            } else {
                CommentDTO parent = commentMap.get(dto.getParentCommentId());
                if (parent != null) {
                    if (parent.getReplies() == null) {
                        parent.setReplies(new java.util.ArrayList<>());
                    }
                    parent.getReplies().add(dto);
                }
            }
        }

        return rootComments;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            throw new BusinessException("评论不存在");
        }

        Comment comment = commentOpt.get();
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评论");
        }

        commentRepository.delete(comment);
    }

    /**
     * 转换为DTO
     */
    private CommentDTO convertToDTO(Comment comment, boolean includeReplies) {
        // 获取评论用户信息
        Optional<com.greengrassland.entity.User> userOpt = userRepository.findById(comment.getUserId());
        String username = userOpt.map(com.greengrassland.entity.User::getUsername).orElse("未知用户");
        String nickname = userOpt.map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername())
                .orElse("未知用户");
        String avatar = userOpt.map(com.greengrassland.entity.User::getAvatar).orElse(null);

        CommentDTO.CommentDTOBuilder builder = CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .username(username)
                .nickname(nickname)
                .avatar(avatar)
                .content(comment.getContent())
                .parentCommentId(comment.getParentCommentId())
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime());

        // 如果有父评论，获取父评论的用户名
        if (comment.getParentCommentId() != null && includeReplies) {
            Optional<Comment> parentCommentOpt = commentRepository.findById(comment.getParentCommentId());
            if (parentCommentOpt.isPresent()) {
                Optional<com.greengrassland.entity.User> parentUserOpt = userRepository.findById(parentCommentOpt.get().getUserId());
                String parentUsername = parentUserOpt.map(com.greengrassland.entity.User::getUsername).orElse(null);
                builder.parentCommentUsername(parentUsername);
            }
        }

        // 如果是顶级评论且需要包含回复，加载子评论
        if (includeReplies && comment.getParentCommentId() == null) {
            List<Comment> replies = commentRepository.findByParentCommentIdOrderByCreateTimeAsc(comment.getId());
            List<CommentDTO> replyDTOs = replies.stream()
                    .map(reply -> convertToDTO(reply, true))
                    .collect(Collectors.toList());
            builder.replies(replyDTOs);
        }

        return builder.build();
    }

    private CommentDTO convertToDTOWithUserMap(Comment comment, java.util.Map<Long, com.greengrassland.entity.User> userMap) {
        com.greengrassland.entity.User user = userMap.get(comment.getUserId());
        String username = user != null ? user.getUsername() : "未知用户";
        String nickname = user != null ? (user.getNickname() != null ? user.getNickname() : user.getUsername()) : "未知用户";
        String avatar = user != null ? user.getAvatar() : null;

        return CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .username(username)
                .nickname(nickname)
                .avatar(avatar)
                .content(comment.getContent())
                .parentCommentId(comment.getParentCommentId())
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .build();
    }
}
