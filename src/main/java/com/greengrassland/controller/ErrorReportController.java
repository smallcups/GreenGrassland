package com.greengrassland.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/error")
public class ErrorReportController {

    @PostMapping("/report")
    public ResponseEntity<?> reportError(@RequestBody String body) {
        log.warn("[Frontend Error] {}", body);
        return ResponseEntity.ok().build();
    }
}
