package com.atguigu.gmall.cart;

import feign.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication()
@EnableFeignClients
@MapperScan("com.atguigu.gmall.cart.mapper")
@ComponentScan(basePackages = "com.atguigu.gmall",excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION,classes = {Aspect.class}))
@EnableAsync
public class GmallCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallCartApplication.class, args);
    }
    @Bean
    Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }
}
