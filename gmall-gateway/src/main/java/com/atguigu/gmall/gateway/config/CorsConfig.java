package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @program: gmall
 * @description: 解决跨域问题
 * @author: 刘广典
 * @create: 2021-01-19 10:24
 **/
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){
        CorsConfiguration config=new CorsConfiguration();
//        允许的域名
        config.addAllowedOrigin("http://manager.gmall.com");
        config.addAllowedOrigin("http://api.gmall.com");
//        允许的方法
        config.addAllowedMethod("*");
//        允许携带自定义请求头
        config.addAllowedHeader("*");
//        允许携带cookie
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource configurationSource=new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",config);
        return new CorsWebFilter(configurationSource);
    }

}
