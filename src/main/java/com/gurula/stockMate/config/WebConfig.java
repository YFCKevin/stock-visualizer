package com.gurula.stockMate.config;

import com.gurula.stockMate.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginInterceptor loginInterceptor;
    private final ConfigProperties configProperties;

    public WebConfig(LoginInterceptor loginInterceptor, ConfigProperties configProperties) {
        this.loginInterceptor = loginInterceptor;
        this.configProperties = configProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/stock/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + this.configProperties.getPicSavePath());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login.html",
                        "/layout.html",
                        "/index.html",
                        "/study-list.html",
                        "/add-study.html",
                        "/callback"
                )
                .excludePathPatterns("/admin/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/images/**", "/webfonts/**", "/fonts/**", "/file/**", "/image/**", "/v3/api-docs/**", "/swagger-ui/**");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        AntPathMatcher matcher = new AntPathMatcher();
        matcher.setCaseSensitive(false);
        configurer.setPathMatcher(matcher);
        configurer.setUseTrailingSlashMatch(true);
    }
}

