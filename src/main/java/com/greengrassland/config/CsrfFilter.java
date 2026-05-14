package com.greengrassland.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String method = request.getMethod();
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            chain.doFilter(req, res);
            return;
        }

        String origin = request.getHeader("Origin");
        String referer = request.getHeader("Referer");
        String serverName = request.getServerName();

        if (origin != null && (origin.contains(serverName) || origin.contains("localhost"))) {
            chain.doFilter(req, res);
            return;
        }
        if (referer != null && (referer.contains(serverName) || referer.contains("localhost"))) {
            chain.doFilter(req, res);
            return;
        }
        // Allow requests without Origin/Referer (mobile apps, curl)
        if (origin == null && referer == null) {
            chain.doFilter(req, res);
            return;
        }

        response.setStatus(403);
        response.getWriter().write("{\"code\":403,\"message\":\"CSRF校验失败\"}");
    }
}
