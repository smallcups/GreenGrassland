package com.greengrassland.service;

import com.greengrassland.dto.PostCreateDTO;
import com.greengrassland.dto.PostDTO;
import com.greengrassland.dto.PostSearchDTO;

import java.util.List;

/**
 * 活动服务接口
 */
public interface PostService {

    /**
     * 发布活动
     */
    PostDTO createPost(Long userId, PostCreateDTO createDTO);

    /**
     * 获取活动列表
     */
    List<PostDTO> getPostList(Long currentUserId);

    /**
     * 搜索活动（带分页）
     */
    com.greengrassland.dto.PageDTO<PostDTO> searchPosts(PostSearchDTO searchDTO, Long currentUserId);

    /**
     * 获取活动详情
     */
    PostDTO getPostDetail(Long postId, Long currentUserId);

    /**
     * 获取我的发布
     */
    List<PostDTO> getMyPosts(Long userId);

    /**
     * 获取我的报名
     */
    List<PostDTO> getMyRegistrations(Long userId);

    /**
     * 删除活动（只能删除自己的）
     */
    void deletePost(Long postId, Long userId);

    /**
     * 取消活动
     */
    void cancelPost(Long postId, Long userId);
}
