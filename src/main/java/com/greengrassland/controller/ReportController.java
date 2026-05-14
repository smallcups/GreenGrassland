package com.greengrassland.controller;

import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.entity.Report;
import com.greengrassland.repository.ReportRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportRepository reportRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> submitReport(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) return ResponseEntity.ok(ApiResponse.error("请先登录"));
        String targetType = (String) body.get("targetType");
        Long targetId = body.get("targetId") instanceof Number ? ((Number) body.get("targetId")).longValue() : null;
        String reason = (String) body.get("reason");
        if (targetType == null || targetId == null) return ResponseEntity.ok(ApiResponse.error("参数不完整"));
        reportRepository.save(Report.builder().reporterId(userId).targetType(targetType).targetId(targetId).reason(reason).build());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
