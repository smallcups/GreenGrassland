package com.greengrassland.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private final Map<String, long[]> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 60;
    private static final long WINDOW_MS = 60_000;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();

        // 静态资源和 WebSocket 不限流
        if (path.startsWith("/ws") || path.startsWith("/uploads") || path.startsWith("/images")) {
            chain.doFilter(req, res);
            return;
        }

        long now = System.currentTimeMillis();
        String key = ip + ":" + path;
        long[] data = requestCounts.computeIfAbsent(key, k -> new long[]{0, now});

        synchronized (data) {
            if (now - data[1] > WINDOW_MS) {
                data[0] = 0;
                data[1] = now;
            }
            data[0]++;
            if (data[0] > MAX_REQUESTS) {
                HttpServletResponse response = (HttpServletResponse) res;
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"data\":null}");
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
