package com.greengrassland.service;

import com.greengrassland.dto.CommentCreateDTO;
import com.greengrassland.dto.CommentDTO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 创建评论
     */
    CommentDTO createComment(Long userId, CommentCreateDTO createDTO);

    /**
     * 获取活动的评论列表
     */
    List<CommentDTO> getCommentsByPostId(Long postId);

    /**
     * 删除评论（只能删除自己的）
     */
    void deleteComment(Long commentId, Long userId);
}
