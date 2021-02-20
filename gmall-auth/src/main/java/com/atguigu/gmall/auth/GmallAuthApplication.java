package com.atguigu.gmall.auth;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableFeignClients
@EnableSwagger2
@ComponentScan(basePackages = "com.atguigu.gmall",excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION,classes = Aspect.class)})
public class GmallAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallAuthApplication.class, args);
    }

}
