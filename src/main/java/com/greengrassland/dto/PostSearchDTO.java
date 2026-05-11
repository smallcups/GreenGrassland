package com.greengrassland.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子搜索DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchDTO {

    /**
     * 搜索关键词（模糊搜索标题和内容）
     */
    private String keyword;

    /**
     * 位置搜索（模糊匹配）
     */
    private String location;

    private Double userLat;
    private Double userLng;
    private Double maxDistance;

    /**
     * 活动类型
     */
    private String type;

    /**
     * 排序字段：createTime（创建时间）、activityTime（活动时间）
     */
    private String sortBy;

    /**
     * 排序方向：ASC（升序）、DESC（降序）
     */
    private String sortOrder;

    /**
     * 页码（从1开始）
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页数量
     */
    @Builder.Default
    private Integer pageSize = 20;
}
