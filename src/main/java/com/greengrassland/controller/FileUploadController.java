package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.exception.BusinessException;
import com.greengrassland.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final UserService userService;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                             HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("文件名为空"));
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            return ResponseEntity.ok(ApiResponse.error("不支持的文件类型，仅支持jpg、jpeg、png、gif、webp"));
        }

        // 验证文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.ok(ApiResponse.error("文件大小不能超过5MB"));
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir, "avatars");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // 保存文件
            file.transferTo(filePath.toFile());

            // 构建访问URL
            String avatarUrl = urlPrefix + "/avatars/" + filename;

            // 更新用户头像
            userService.updateAvatar(userId, avatarUrl);

            return ResponseEntity.ok(ApiResponse.success(avatarUrl));
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/post-image")
    public ResponseEntity<ApiResponse<String>> uploadPostImage(@RequestParam("file") MultipartFile file,
                                                                HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("文件名为空"));
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            return ResponseEntity.ok(ApiResponse.error("不支持的文件类型，仅支持jpg、jpeg、png、gif、webp"));
        }

        // 验证文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.ok(ApiResponse.error("文件大小不能超过5MB"));
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir, "posts");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // 保存文件
            file.transferTo(filePath.toFile());

            // 构建访问URL
            String imageUrl = urlPrefix + "/posts/" + filename;

            return ResponseEntity.ok(ApiResponse.success(imageUrl));
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

}
