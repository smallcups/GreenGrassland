package com.greengrassland.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/error")
public class ErrorReportController {

    @PostMapping("/report")
    public ResponseEntity<?> reportError(@RequestBody Map<String, Object> body) {
        log.warn("[Frontend Error] {}", body);
        return ResponseEntity.ok().build();
    }
}
