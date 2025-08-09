package com.gurula.stockMate.interceptor;

import com.gurula.stockMate.config.ConfigProperties;
import com.gurula.stockMate.oauth.Role;
import com.gurula.stockMate.member.MemberContext;
import com.gurula.stockMate.member.MemberService;
import com.gurula.stockMate.oauth.JwtTool;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Configuration
public class LoginInterceptor implements HandlerInterceptor{
    private final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
    private final ConfigProperties configProperties;
    private final JwtTool jwtTool;
    private final MemberService memberService;

    public LoginInterceptor(ConfigProperties configProperties, JwtTool jwtTool, MemberService memberService) {
        this.configProperties = configProperties;
        this.jwtTool = jwtTool;
        this.memberService = memberService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handle) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        logger.info("Request URL: " + requestURI + ", Method: " + method);

        // 1. 從 Cookie 取得 Bearer Token
        String token = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> "JWT_TOKEN".equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .findFirst())
                .orElse(null);

        // 2. 若 Cookie 沒有，從 Authorization header 嘗試取得 Bearer Token
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        System.out.println("token = " + token);

        // 取出 token 內的 memberId
        final String memberId = jwtTool.parseToken(token);

        return memberService.findById(memberId)
                .map(member -> {
                    if (Role.ADMIN.equals(member.getRole()) && requestURI.startsWith("/admin/")) {
                        return true;
                    }
                    // 如果角色不是 ADMIN 且訪問 /admin/，拒絕並跳轉
                    if (!Role.ADMIN.equals(member.getRole()) && requestURI.startsWith("/admin/")) {
                        return denyAccess(response);
                    }
                    MemberContext.setMember(member);
                    return true;
                })
                .orElseGet(() -> {
                    logger.warn("未登入索取的資源是：{}", request.getRequestURI());
                    try {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"message\": \"Unauthorized\"}");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return false;
                });
    }


    private boolean denyAccess(HttpServletResponse response) {
        logger.warn("非管理員嘗試訪問受限資源");
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 設定 401
            response.getWriter().write("Unauthorized access");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}
