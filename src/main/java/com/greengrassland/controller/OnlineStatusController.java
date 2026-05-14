package com.greengrassland.controller;

import com.greengrassland.dto.ApiResponse;
import com.greengrassland.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/online")
@RequiredArgsConstructor
public class OnlineStatusController {

    private final OnlineStatusService onlineStatusService;

    @GetMapping("/status/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkOnline(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(onlineStatusService.isOnline(userId)));
    }
}
