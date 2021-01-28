package com.atguigu.gmall.search;


import feign.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
 * @program: gmall
 * @description: 启动类
 * @author: lgd
 * @create: 2021-01-28 19:41
 **/
@SpringBootApplication
@EnableFeignClients
public class GmallSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallSearchApplication.class,args);
    }

    /**
     *   @desc 配置feign的日志
     *   @auth 刘广典
     *   @Date 2020/12/21 19:17
     **/
    @Bean
    Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }
}
