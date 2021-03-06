package com.atguigu.gmall.msg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GmallMsgApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallMsgApplication.class, args);
    }

}
