package com.atguigu.gmall.pms.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-07 00:40
 **/
@Configuration
public class RedisConfig {

    @Bean
    public RedissonClient redissonClient(){
        Config config =new Config();
        config.useSingleServer().setAddress("redis://111.1.1.11:6379");
        return Redisson.create(config);
    }
}
