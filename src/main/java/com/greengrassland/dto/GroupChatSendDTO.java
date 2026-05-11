package com.greengrassland.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送群消息DTO
 */
@Data
public class GroupChatSendDTO {

    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
