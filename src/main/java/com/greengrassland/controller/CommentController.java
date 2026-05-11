package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.dto.CommentCreateDTO;
import com.greengrassland.dto.CommentDTO;
import com.greengrassland.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 创建评论
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(@Valid @RequestBody CommentCreateDTO createDTO,
                                                                  HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        CommentDTO commentDTO = commentService.createComment(userId, createDTO);
        return ResponseEntity.ok(ApiResponse.success(commentDTO));
    }

    /**
     * 获取活动的评论列表
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getComments(@PathVariable Long postId) {
        List<CommentDTO> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteComment(@PathVariable Long id,
                                                         HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        commentService.deleteComment(id, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
