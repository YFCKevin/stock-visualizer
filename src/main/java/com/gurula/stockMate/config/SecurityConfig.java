package com.gurula.stockMate.config;

import com.gurula.stockMate.oauth.CustomOAuth2UserService;
import com.gurula.stockMate.oauth.OauthLoginFailureHandler;
import com.gurula.stockMate.oauth.OauthLoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService oauthUserService;

    private final OauthLoginSuccessHandler oauthLoginSuccessHandler;  //第三方登入成功後會處理的

    @Autowired
    OauthLoginFailureHandler oauthLoginFailureHandler;  //第三方登入失敗或取消會進來處理

    private final JwtProperties jwtProperties;

    public SecurityConfig(JwtProperties jwtProperties, @Lazy OauthLoginSuccessHandler oauthLoginSuccessHandler) {
        this.jwtProperties = jwtProperties;
        this.oauthLoginSuccessHandler = oauthLoginSuccessHandler;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers().disable();
        http.headers().frameOptions().disable();
        http.csrf(csrf -> csrf.disable());

        http.logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\": \"登出成功\"}");
                })
                .and().oauth2Login().userInfoEndpoint()
                .userService(oauthUserService)
                .and()
                .successHandler(oauthLoginSuccessHandler)
                .failureHandler(oauthLoginFailureHandler);

        return http.build();
    }


    @Bean
    public KeyPair keyPair() throws Exception {
        // 載入 Keystore
        Resource keystoreResource = new org.springframework.core.io.ClassPathResource(jwtProperties.getLocation());
        try (InputStream is = keystoreResource.getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(is, jwtProperties.getPassword().toCharArray());

            // 獲取私鑰
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(jwtProperties.getAlias(), jwtProperties.getPassword().toCharArray());
            if (privateKey == null) {
                throw new RuntimeException("未找到指定 alias 的私鑰: " + jwtProperties.getAlias());
            }

            // 獲取公鑰
            Certificate cert = keyStore.getCertificate(jwtProperties.getAlias());
            if (cert == null) {
                throw new RuntimeException("未找到指定 alias 的證書: " + jwtProperties.getAlias());
            }
            PublicKey publicKey = cert.getPublicKey();

            return new KeyPair(publicKey, privateKey);
        }
    }

}
