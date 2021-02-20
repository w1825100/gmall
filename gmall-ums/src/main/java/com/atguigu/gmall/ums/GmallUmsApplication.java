package com.atguigu.gmall.ums;

import org.aspectj.lang.annotation.Aspect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableFeignClients
@EnableSwagger2
@EnableDiscoveryClient
@MapperScan("com.atguigu.gmall.ums.mapper")
@ComponentScan(basePackages = "com.atguigu.gmall",
        //指定要过滤的内容
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION,classes = {Aspect.class})})
public class GmallUmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallUmsApplication.class, args);
    }

}
