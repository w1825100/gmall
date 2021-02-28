package com.atguigu.gmall.order.config;


import com.atguigu.gmall.order.interceptor.OrderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-21 13:37
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private OrderInterceptor orderInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderInterceptor).addPathPatterns("/**");
    }
}
