package com.greengrassland.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Session拦截器
 * 用于管理用户登录状态
 */
@Component
public class SessionConfig implements HandlerInterceptor {

    public static final String SESSION_USER_ID = "userId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return true;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userId = session.getAttribute(SESSION_USER_ID);
        return userId != null ? (Long) userId : null;
    }
}
