package com.greengrassland.dto;

import com.greengrassland.entity.PostType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建活动DTO
 */
@Data
public class PostCreateDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    @NotNull(message = "活动类型不能为空")
    private PostType type;

    @NotNull(message = "最大人数不能为空")
    @Min(value = 1, message = "最大人数必须大于0")
    private Integer maxPeople;

    private LocalDateTime activityTime;

    private String location;

    /**
     * 图片URL列表（多个图片用逗号分隔）
     */
    private String images;
}
