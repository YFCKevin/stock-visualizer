package com.gurula.stockMate.oauth;

import com.gurula.stockMate.config.ConfigProperties;
import com.gurula.stockMate.member.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class OauthLoginSuccessHandler implements AuthenticationSuccessHandler {
    protected Logger logger = LoggerFactory.getLogger(OauthLoginSuccessHandler.class);
    private final UserService userService;
    private final ConfigProperties configProperties;
    private final JwtTool jwtTool;

    public OauthLoginSuccessHandler(UserService userService, ConfigProperties configProperties, JwtTool jwtTool) {
        this.userService = userService;
        this.configProperties = configProperties;
        this.jwtTool = jwtTool;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        System.out.println("第三方登入成功後要做的");

        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        System.out.println(oauthUser.getOauth2ClientName());

        //處理把第三方的帳號儲存到DB，Oauth2ClientName是GOOGLE、FACEBOOK、LINE.....
        Member member  = userService.processOAuthPostLogin(oauthUser.getEmail(),oauthUser.getName(),oauthUser.getOauth2ClientName());

        // 製作 JWT token
        final String token = jwtTool.createToken(member.getId(), Duration.ofDays(365));
        System.out.println("token = " + token);

        // 設定 Cookie
        ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token)
                .secure(true)
                .path("/")
                .maxAge(30790400)   // 365天
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", cookie.toString());

        response.sendRedirect(configProperties.getGlobalDomain() + "index.html");
    }

}
