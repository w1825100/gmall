package com.atguigu.gmall.job;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.atguigu.gmall.job.mapper")
public class GmallJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallJobApplication.class, args);
    }

}
