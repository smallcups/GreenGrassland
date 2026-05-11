package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.dto.PostCreateDTO;
import com.greengrassland.dto.PostDTO;
import com.greengrassland.dto.PostSearchDTO;
import com.greengrassland.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 发布活动
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostDTO>> createPost(@Valid @RequestBody PostCreateDTO createDTO,
                                                            HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        PostDTO postDTO = postService.createPost(userId, createDTO);
        return ResponseEntity.ok(ApiResponse.success(postDTO));
    }

    /**
     * 获取活动列表（支持搜索和排序）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPostList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);

        // 始终使用分页接口
        PostSearchDTO searchDTO = PostSearchDTO.builder()
                .keyword(keyword != null && !keyword.isEmpty() ? keyword : null)
                .location(location != null && !location.isEmpty() ? location : null)
                .type(type != null && !type.isEmpty() ? type : null)
                .sortBy(sortBy != null ? sortBy : "createTime")
                .sortOrder(sortOrder != null ? sortOrder : "DESC")
                .page(page != null ? page : 1)
                .pageSize(pageSize != null ? pageSize : 12)
                .build();
        return ResponseEntity.ok(ApiResponse.success(postService.searchPosts(searchDTO, userId)));
    }

    /**
     * 搜索活动（POST 分页）
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<com.greengrassland.dto.PageDTO<PostDTO>>> searchPosts(@RequestBody PostSearchDTO searchDTO,
                                                                    HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        return ResponseEntity.ok(ApiResponse.success(postService.searchPosts(searchDTO, userId)));
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDTO>> getPostDetail(@PathVariable Long id,
                                                               HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        PostDTO postDTO = postService.getPostDetail(id, userId);
        return ResponseEntity.ok(ApiResponse.success(postDTO));
    }

    /**
     * 获取我的发布
     */
    @GetMapping("/my/posts")
    public ResponseEntity<ApiResponse<List<PostDTO>>> getMyPosts(HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        List<PostDTO> posts = postService.getMyPosts(userId);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * 获取我的报名
     */
    @GetMapping("/my/registrations")
    public ResponseEntity<ApiResponse<List<PostDTO>>> getMyRegistrations(HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        List<PostDTO> posts = postService.getMyRegistrations(userId);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePost(@PathVariable Long id,
                                                      HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        postService.deletePost(id, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
