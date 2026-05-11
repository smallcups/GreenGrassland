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

    @NotBlank(message = "活动地点不能为空")
    private String location;

    private Double latitude;
    private Double longitude;

    private String images;
}
