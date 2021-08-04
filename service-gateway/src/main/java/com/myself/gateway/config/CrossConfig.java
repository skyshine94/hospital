package com.myself.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * 跨域配置类
 *
 * @author Wei
 * @since 2021/7/3
 */
@Configuration
public class CrossConfig {

    @Bean
    public CorsWebFilter coreWebFilter(){
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedMethod("*"); //允许提交的请求方式
        config.addAllowedOrigin("*"); //允许提交的url
        config.addAllowedHeader("*"); //允许提交的头信息
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config); //允许提交的请求路径
        return new CorsWebFilter(source);
    }
}
