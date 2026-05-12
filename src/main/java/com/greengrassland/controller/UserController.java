package com.greengrassland.controller;

import com.greengrassland.config.JwtUtil;
import com.greengrassland.config.SessionConfig;
import com.greengrassland.dto.ApiResponse;
import com.greengrassland.dto.UserDTO;
import com.greengrassland.dto.UserLoginDTO;
import com.greengrassland.dto.UserRegisterDTO;
import com.greengrassland.dto.UserPasswordDTO;
import com.greengrassland.dto.UserUpdateDTO;
import com.greengrassland.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserDTO userDTO = userService.register(registerDTO);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDTO>> login(@Valid @RequestBody UserLoginDTO loginDTO,
                                                       HttpServletRequest request) {
        UserDTO userDTO = userService.login(loginDTO);
        
        // 将用户ID存入Session
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionConfig.SESSION_USER_ID, userDTO.getId());

        // 生成JWT token
        userDTO.setToken(jwtUtil.generateToken(userDTO.getId()));

        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("未登录"));
        }
        
        UserDTO userDTO = userService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long userId) {
        UserDTO userDTO = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    /**
     * 更新用户资料
     */
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@Valid @RequestBody UserUpdateDTO updateDTO,
                                                               HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        UserDTO userDTO = userService.updateProfile(userId, updateDTO);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    public ResponseEntity<ApiResponse<?>> updatePassword(@Valid @RequestBody UserPasswordDTO passwordDTO,
                                                          HttpServletRequest request) {
        Long userId = SessionConfig.getCurrentUserId(request);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.error("请先登录"));
        }

        userService.updatePassword(userId, passwordDTO);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
